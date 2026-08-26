package com.ai.shrija.leave.agent.service;

import com.ai.shrija.leave.agent.dto.ApplyLeaveRequest;
import com.ai.shrija.leave.agent.exception.InsufficientLeaveException;
import com.ai.shrija.leave.agent.exception.LeaveNotFoundException;
import com.ai.shrija.leave.agent.exception.LeaveValidationException;
import com.ai.shrija.leave.agent.mcp.LeaveMcpClient;
import com.ai.shrija.leave.agent.model.Leave;
import com.ai.shrija.leave.agent.util.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory leave ledger and business rules. Swap the internal maps for a
 * repository (JPA/JDBC) backed by the real HR datastore in production; the
 * public API here is what the tools and controller depend on.
 */
@Service
public class LeaveService {

    private final LeaveMcpClient leaveMcpClient;
    private final Map<String, Leave> leavesById = new ConcurrentHashMap<>();

    /** employeeId -> (leaveType -> remaining days) */
    private final Map<String, Map<Leave.Type, Double>> balances = new ConcurrentHashMap<>();

    private static final Map<Leave.Type, Double> DEFAULT_ANNUAL_ALLOWANCE = Map.of(
            Leave.Type.ANNUAL, 20.0,
            Leave.Type.SICK, 12.0,
            Leave.Type.CASUAL, 6.0,
            Leave.Type.UNPAID, Double.POSITIVE_INFINITY,
            Leave.Type.MATERNITY, 180.0,
            Leave.Type.PATERNITY, 15.0
    );

    public LeaveService(LeaveMcpClient leaveMcpClient) {
        this.leaveMcpClient = leaveMcpClient;
    }

    public Leave applyLeave(ApplyLeaveRequest request) {
        if (request.getEmployeeId() == null || request.getEmployeeId().isBlank()) {
            throw new LeaveValidationException("employeeId is required");
        }
        double requestedDays = DateUtils.countBusinessDays(request.getStartDate(), request.getEndDate());
        if (requestedDays <= 0) {
            throw new LeaveValidationException("Leave period must include at least one business day");
        }

        double available = getBalance(request.getEmployeeId(), request.getType());
        if (requestedDays > available) {
            throw new InsufficientLeaveException(
                    request.getEmployeeId(), request.getType().name(), requestedDays, available);
        }

        Leave leave = new Leave();
        leave.setEmployeeId(request.getEmployeeId());
        leave.setType(request.getType());
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setNumberOfDays(requestedDays);
        leave.setReason(request.getReason());
        leave.setStatus(Leave.Status.PENDING_APPROVAL);

        // HRMS MCP lookup: flag (but don't block) leave that starts on a
        // known company holiday, since the requester may not be aware of it.
        // A down/unreachable HRMS server degrades to "unknown" (false) here
        // rather than failing the whole application — see LeaveMcpClient.
        if (leaveMcpClient.isCompanyHoliday(request.getStartDate().toString())) {
            leave.setStartsOnCompanyHoliday(true);
        }

        leavesById.put(leave.getLeaveId(), leave);
        return leave;
    }

    public Leave cancelLeave(String leaveId, String requestedBy) {
        Leave leave = getLeave(leaveId);
        if (leave.getStatus() == Leave.Status.CANCELLED) {
            return leave;
        }
        if (leave.getStatus() == Leave.Status.APPROVED) {
            // refund the days back to the balance since it was already deducted on approval
            creditBalance(leave.getEmployeeId(), leave.getType(), leave.getNumberOfDays());
        }
        leave.setStatus(Leave.Status.CANCELLED);
        return leave;
    }

    public Leave decideApproval(String leaveId, String approverId, boolean approved, String comments) {
        Leave leave = getLeave(leaveId);
        if (leave.getStatus() != Leave.Status.PENDING_APPROVAL) {
            throw new LeaveValidationException(
                    "Leave " + leaveId + " is not pending approval (current status: " + leave.getStatus() + ")");
        }
        leave.setApproverId(approverId);
        leave.setApproverComments(comments);

        if (approved) {
            debitBalance(leave.getEmployeeId(), leave.getType(), leave.getNumberOfDays());
            leave.setStatus(Leave.Status.APPROVED);
        } else {
            leave.setStatus(Leave.Status.REJECTED);
        }
        return leave;
    }

    public Leave getLeave(String leaveId) {
        Leave leave = leavesById.get(leaveId);
        if (leave == null) {
            throw new LeaveNotFoundException(leaveId);
        }
        return leave;
    }

    public Map<String, Double> getAllBalances(String employeeId) {
        Map<Leave.Type, Double> employeeBalances = balances.computeIfAbsent(
                employeeId, id -> new ConcurrentHashMap<>(DEFAULT_ANNUAL_ALLOWANCE));
        return employeeBalances.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
    }

    public double getBalance(String employeeId, Leave.Type type) {
        return balances
                .computeIfAbsent(employeeId, id -> new ConcurrentHashMap<>(DEFAULT_ANNUAL_ALLOWANCE))
                .getOrDefault(type, 0.0);
    }

    private void debitBalance(String employeeId, Leave.Type type, double days) {
        balances.computeIfAbsent(employeeId, id -> new ConcurrentHashMap<>(DEFAULT_ANNUAL_ALLOWANCE))
                .merge(type, -days, Double::sum);
    }

    private void creditBalance(String employeeId, Leave.Type type, double days) {
        balances.computeIfAbsent(employeeId, id -> new ConcurrentHashMap<>(DEFAULT_ANNUAL_ALLOWANCE))
                .merge(type, days, Double::sum);
    }
}
