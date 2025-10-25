package com.achieveil.bizcore.service;

import com.achieveil.bizcore.dto.StatisticsDTO;
import com.achieveil.bizcore.entity.Employee;
import com.achieveil.bizcore.entity.User;
import com.achieveil.bizcore.repository.DepartmentRepository;
import com.achieveil.bizcore.repository.EmployeeRepository;
import com.achieveil.bizcore.repository.UserRepository;
import com.achieveil.bizcore.util.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByEmpNo(employee.getEmpNo())) {
            throw new RuntimeException("工号已存在");
        }

        // 如果已经设置userId，验证关联的用户是否存在
        if (employee.getUserId() != null && !userRepository.existsById(employee.getUserId())) {
            throw new RuntimeException("关联的用户不存在");
        }

        Employee saved = employeeRepository.save(employee);
        enrichEmployee(saved);
        return saved;
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee employee) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        // 如果工号改变，检查新工号是否已存在
        if (!existingEmployee.getEmpNo().equals(employee.getEmpNo())
            && employeeRepository.existsByEmpNo(employee.getEmpNo())) {
            throw new RuntimeException("工号已存在");
        }

        existingEmployee.setEmpNo(employee.getEmpNo());
        existingEmployee.setName(employee.getName());
        existingEmployee.setGender(employee.getGender());
        existingEmployee.setBirthDate(employee.getBirthDate());
        existingEmployee.setIdCard(employee.getIdCard());
        existingEmployee.setPhone(employee.getPhone());
        existingEmployee.setEmail(employee.getEmail());
        existingEmployee.setDeptId(employee.getDeptId());
        existingEmployee.setPosition(employee.getPosition());
        existingEmployee.setLevel(employee.getLevel());
        existingEmployee.setHireDate(employee.getHireDate());
        existingEmployee.setSalary(employee.getSalary());
        existingEmployee.setAddress(employee.getAddress());
        existingEmployee.setStatus(employee.getStatus());

        Employee savedEmployee = employeeRepository.save(existingEmployee);

        // 同步更新关联用户信息
        if (savedEmployee.getUserId() != null) {
            User user = userRepository.findById(savedEmployee.getUserId())
                    .orElseThrow(() -> new RuntimeException("关联的用户账户不存在"));

            // 如果更新了邮箱，需要确保唯一性
            if (StringUtils.hasText(savedEmployee.getEmail())) {
                userRepository.findByEmail(savedEmployee.getEmail()).ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(user.getId())) {
                        throw new RuntimeException("邮箱已被使用");
                    }
                });
            } else {
                throw new RuntimeException("已关联账户的员工邮箱不能为空");
            }

            user.setRealName(savedEmployee.getName());
            user.setPhone(savedEmployee.getPhone());
            user.setEmail(savedEmployee.getEmail());
            user.setStatus(savedEmployee.getStatus());
            userRepository.save(user);
        }

        enrichEmployee(savedEmployee);
        return savedEmployee;
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        Long userId = employee.getUserId();
        employeeRepository.delete(employee);

        if (userId != null && userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
        }
    }

    public Employee getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));
        enrichEmployee(employee);
        return employee;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdTime"));
        enrichEmployees(employees);
        return employees;
    }

    public Page<Employee> searchEmployees(String keyword, Long deptId, Integer status,
                                          int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));

        Specification<Employee> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                Predicate namePredicate = cb.like(root.get("name"), "%" + keyword + "%");
                Predicate empNoPredicate = cb.like(root.get("empNo"), "%" + keyword + "%");
                predicates.add(cb.or(namePredicate, empNoPredicate));
            }

            if (deptId != null) {
                predicates.add(cb.equal(root.get("deptId"), deptId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return employeeRepository.findAll(spec, pageable)
                .map(employee -> {
                    enrichEmployee(employee);
                    return employee;
                });
    }

    public List<Employee> getEmployeesByDeptId(Long deptId) {
        List<Employee> employees = employeeRepository.findByDeptId(deptId);
        enrichEmployees(employees);
        return employees;
    }

    public long getTotalCount() {
        return employeeRepository.count();
    }

    public long getActiveEmployeeCount() {
        return employeeRepository.countByStatus(1);
    }

    public long getEmployeeCountByDept(Long deptId) {
        return employeeRepository.countByDeptId(deptId);
    }

    // 性别分布统计
    public List<StatisticsDTO> getGenderStatistics() {
        List<Employee> employees = employeeRepository.findAll();
        Map<String, Long> genderMap = employees.stream()
                .filter(e -> e.getGender() != null)
                .collect(Collectors.groupingBy(Employee::getGender, Collectors.counting()));

        return genderMap.entrySet().stream()
                .map(entry -> new StatisticsDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    // 部门员工数量统计
    public List<StatisticsDTO> getDepartmentStatistics() {
        List<Employee> employees = employeeRepository.findByStatus(1);
        Map<Long, Long> deptMap = employees.stream()
                .filter(e -> e.getDeptId() != null)
                .collect(Collectors.groupingBy(Employee::getDeptId, Collectors.counting()));

        return deptMap.entrySet().stream()
                .map(entry -> {
                    String deptName = departmentRepository.findById(entry.getKey())
                            .map(d -> d.getDeptName())
                            .orElse("未知部门");
                    return new StatisticsDTO(deptName, entry.getValue());
                })
                .collect(Collectors.toList());
    }

    // 薪资分布统计
    public Map<String, Long> getSalaryStatistics() {
        List<Employee> employees = employeeRepository.findByStatus(1);
        Map<String, Long> salaryMap = new HashMap<>();

        long below5k = employees.stream()
                .filter(e -> e.getSalary() != null && e.getSalary().compareTo(new BigDecimal("5000")) < 0)
                .count();
        long between5kAnd10k = employees.stream()
                .filter(e -> e.getSalary() != null
                        && e.getSalary().compareTo(new BigDecimal("5000")) >= 0
                        && e.getSalary().compareTo(new BigDecimal("10000")) < 0)
                .count();
        long between10kAnd20k = employees.stream()
                .filter(e -> e.getSalary() != null
                        && e.getSalary().compareTo(new BigDecimal("10000")) >= 0
                        && e.getSalary().compareTo(new BigDecimal("20000")) < 0)
                .count();
        long above20k = employees.stream()
                .filter(e -> e.getSalary() != null && e.getSalary().compareTo(new BigDecimal("20000")) >= 0)
                .count();

        salaryMap.put("5K以下", below5k);
        salaryMap.put("5K-10K", between5kAnd10k);
        salaryMap.put("10K-20K", between10kAnd20k);
        salaryMap.put("20K以上", above20k);

        return salaryMap;
    }

    // 入职趋势统计（按月）
    public Map<String, Long> getHireTrendStatistics() {
        List<Employee> employees = employeeRepository.findAll();
        Map<String, Long> trendMap = employees.stream()
                .filter(e -> e.getHireDate() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getHireDate().getYear() + "-" + String.format("%02d", e.getHireDate().getMonthValue()),
                        Collectors.counting()
                ));

        return trendMap;
    }

    /**
     * 创建员工并自动创建用户账户
     */
    @Transactional
    public Employee createEmployeeWithAccount(Employee employee, String password, String securityQuestion, String securityAnswer) {
        // 检查工号是否存在
        if (employeeRepository.existsByEmpNo(employee.getEmpNo())) {
            throw new RuntimeException("工号已存在");
        }

        // 保存员工信息
        Employee savedEmployee = employeeRepository.save(employee);

        // 为员工注册账号
        return registerEmployeeAccount(savedEmployee.getId(), password, securityQuestion, securityAnswer);
    }

    /**
     * 变更员工角色（EMPLOYEE <-> MANAGER）
     */
    @Transactional
    public void changeEmployeeRole(Long empId, String newRole) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        if (employee.getUserId() == null) {
            throw new RuntimeException("该员工没有关联的用户账户");
        }

        // 验证角色
        if (!newRole.equals("EMPLOYEE") && !newRole.equals("MANAGER")) {
            throw new RuntimeException("无效的角色，只能是EMPLOYEE或MANAGER");
        }

        User user = userRepository.findById(employee.getUserId())
                .orElseThrow(() -> new RuntimeException("关联的用户账户不存在"));

        // 更新用户角色
        user.setRole(newRole);
        userRepository.save(user);
    }

    /**
     * 根据userId获取员工信息
     */
    public Employee getEmployeeByUserId(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElse(null);
        enrichEmployee(employee);
        return employee;
    }

    /**
     * 获取所有经理（role为MANAGER的员工）
     */
    public List<Employee> getAllManagers() {
        // 查找所有role为MANAGER的用户
        List<User> managerUsers = userRepository.findByRole("MANAGER");
        List<Long> managerUserIds = managerUsers.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        // 查找这些用户对应的员工
        List<Employee> managers = employeeRepository.findByUserIdIn(managerUserIds);
        enrichEmployees(managers);
        return managers;
    }

    /**
     * 为员工注册账号
     */
    @Transactional
    public Employee registerEmployeeAccount(Long employeeId, String password, String securityQuestion, String securityAnswer) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        if (employee.getUserId() != null) {
            throw new RuntimeException("该员工已关联用户账户");
        }

        if (!StringUtils.hasText(employee.getEmail())) {
            throw new RuntimeException("请先为员工填写邮箱再注册账号");
        }

        if (!StringUtils.hasText(securityQuestion) || !StringUtils.hasText(securityAnswer)) {
            throw new RuntimeException("安全问题和答案不能为空");
        }

        // 校验用户名和邮箱是否被占用
        if (userRepository.existsByUsername(employee.getEmpNo())) {
            throw new RuntimeException("用户名已存在，请检查工号是否重复");
        }
        userRepository.findByEmail(employee.getEmail()).ifPresent(existingUser -> {
            throw new RuntimeException("邮箱已被使用");
        });

        // 确定实际密码并验证长度
        String actualPassword = StringUtils.hasText(password) ? password : "PASSword123";
        PasswordPolicy.ensureWithinSupportedLength(actualPassword);

        User user = new User();
        user.setUsername(employee.getEmpNo());
        user.setPassword(passwordEncoder.encode(actualPassword));
        user.setRealName(employee.getName());
        user.setEmail(employee.getEmail());
        user.setPhone(employee.getPhone());
        user.setRole("EMPLOYEE");
        user.setStatus(employee.getStatus());
        user.setSecurityQuestion(securityQuestion.trim());
        user.setSecurityAnswer(securityAnswer.trim());

        User savedUser = userRepository.save(user);

        employee.setUserId(savedUser.getId());
        Employee updated = employeeRepository.save(employee);
        enrichEmployee(updated);
        return updated;
    }

    /**
     * 重置员工账号密码
     */
    @Transactional
    public void resetEmployeePassword(Long employeeId, String newPassword) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        if (employee.getUserId() == null) {
            throw new RuntimeException("该员工尚未关联用户账户");
        }

        User user = userRepository.findById(employee.getUserId())
                .orElseThrow(() -> new RuntimeException("关联的用户账户不存在"));

        // 确定实际密码并验证长度
        String actualPassword = StringUtils.hasText(newPassword) ? newPassword : "PASSword123";
        PasswordPolicy.ensureWithinSupportedLength(actualPassword);

        user.setPassword(passwordEncoder.encode(actualPassword));
        userRepository.save(user);
    }

    private void enrichEmployees(Collection<Employee> employees) {
        if (employees == null) {
            return;
        }
        employees.forEach(this::enrichEmployee);
    }

    private void enrichEmployee(Employee employee) {
        if (employee == null) {
            return;
        }
        if (employee.getUserId() != null) {
            userRepository.findById(employee.getUserId()).ifPresent(user -> {
                employee.setUserRole(user.getRole());
                employee.setManager("MANAGER".equalsIgnoreCase(user.getRole()));
            });
        } else {
            employee.setUserRole(null);
            employee.setManager(false);
        }
    }
}
