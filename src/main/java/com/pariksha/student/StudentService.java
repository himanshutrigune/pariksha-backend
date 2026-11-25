package com.pariksha.student;

import com.pariksha.academic.Standard;
import com.pariksha.academic.StandardRepository;
import com.pariksha.common.ConflictException;
import com.pariksha.common.PageResponse;
import com.pariksha.common.ResourceNotFoundException;
import com.pariksha.institute.InstituteRepository;
import com.pariksha.student.StudentDtos.CreateStudentRequest;
import com.pariksha.student.StudentDtos.StudentResponse;
import com.pariksha.student.StudentDtos.UpdateStudentRequest;
import com.pariksha.user.Role;
import com.pariksha.user.User;
import com.pariksha.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final StandardRepository standardRepository;
    private final InstituteRepository instituteRepository;
    private final UserService userService;
    private final Clock clock;

    @Transactional
    public StudentResponse create(Long instituteId, CreateStudentRequest request) {
        String roll = request.rollNumber().trim();
        if (studentRepository.existsByInstituteIdAndRollNumberIgnoreCase(instituteId, roll)) {
            throw new ConflictException("Roll number already used in this institute: " + roll);
        }
        User user = userService.createUser(instituteId, request.name(), request.email(),
                request.mobileNumber(), request.password(), Role.STUDENT);

        Student student = new Student();
        student.setInstitute(instituteRepository.getReferenceById(instituteId));
        student.setUser(user);
        student.setStandard(findStandard(request.standardId()));
        student.setRollNumber(roll);
        student.setGender(request.gender());
        student.setParentContact(request.parentContact());
        student.setJoiningDate(request.joiningDate() != null ? request.joiningDate() : LocalDate.now(clock));
        return StudentResponse.from(studentRepository.save(student));
    }

    public PageResponse<StudentResponse> search(Long instituteId, String search, Pageable pageable) {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        return PageResponse.of(studentRepository.search(instituteId, term, pageable), StudentResponse::from);
    }

    public StudentResponse get(Long instituteId, Long id) {
        return StudentResponse.from(find(instituteId, id));
    }

    @Transactional
    public StudentResponse update(Long instituteId, Long id, UpdateStudentRequest request) {
        Student student = find(instituteId, id);
        String roll = request.rollNumber().trim();
        if (!student.getRollNumber().equalsIgnoreCase(roll)
                && studentRepository.existsByInstituteIdAndRollNumberIgnoreCase(instituteId, roll)) {
            throw new ConflictException("Roll number already used in this institute: " + roll);
        }
        student.getUser().setName(request.name().trim());
        student.getUser().setMobileNumber(request.mobileNumber());
        student.setRollNumber(roll);
        student.setGender(request.gender());
        student.setParentContact(request.parentContact());
        student.setStandard(findStandard(request.standardId()));
        return StudentResponse.from(student);
    }

    /** Soft delete: history (results, attempts) must stay intact, so we only deactivate. */
    @Transactional
    public void deactivate(Long instituteId, Long id) {
        Student student = find(instituteId, id);
        student.setActive(false);
        student.getUser().setActive(false);
    }

    private Student find(Long instituteId, Long id) {
        return studentRepository.findByIdAndInstituteId(id, instituteId)
                .filter(Student::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    private Standard findStandard(Long standardId) {
        if (standardId == null) {
            return null;
        }
        return standardRepository.findById(standardId)
                .orElseThrow(() -> new ResourceNotFoundException("Standard not found: " + standardId));
    }
}
