package com.pariksha.classroom;

import com.pariksha.classroom.ClassroomDtos.ClassroomDetail;
import com.pariksha.classroom.ClassroomDtos.ClassroomRequest;
import com.pariksha.classroom.ClassroomDtos.ClassroomResponse;
import com.pariksha.common.BadRequestException;
import com.pariksha.common.ConflictException;
import com.pariksha.common.ResourceNotFoundException;
import com.pariksha.exam.Exam;
import com.pariksha.exam.ExamRepository;
import com.pariksha.exam.ExamStatus;
import com.pariksha.institute.InstituteRepository;
import com.pariksha.student.Student;
import com.pariksha.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final InstituteRepository instituteRepository;

    @Transactional
    public ClassroomResponse create(Long instituteId, ClassroomRequest request) {
        String name = request.name().trim();
        if (classroomRepository.existsByInstituteIdAndNameIgnoreCase(instituteId, name)) {
            throw new ConflictException("Classroom already exists: " + name);
        }
        Classroom classroom = new Classroom();
        classroom.setInstitute(instituteRepository.getReferenceById(instituteId));
        classroom.setName(name);
        return ClassroomResponse.from(classroomRepository.save(classroom));
    }

    public List<ClassroomResponse> list(Long instituteId) {
        return classroomRepository.findByInstituteIdAndActiveTrueOrderByNameAsc(instituteId)
                .stream().map(ClassroomResponse::from).toList();
    }

    public ClassroomDetail get(Long instituteId, Long id) {
        return ClassroomDetail.from(find(instituteId, id));
    }

    @Transactional
    public ClassroomDetail addStudents(Long instituteId, Long id, Set<Long> studentIds) {
        Classroom classroom = find(instituteId, id);
        List<Student> students = studentRepository.findByIdInAndInstituteIdAndActiveTrue(studentIds, instituteId);
        if (students.size() != studentIds.size()) {
            throw new BadRequestException("One or more students were not found in your institute");
        }
        classroom.getStudents().addAll(students);
        return ClassroomDetail.from(classroom);
    }

    @Transactional
    public ClassroomDetail removeStudent(Long instituteId, Long id, Long studentId) {
        Classroom classroom = find(instituteId, id);
        classroom.getStudents().removeIf(s -> s.getId().equals(studentId));
        return ClassroomDetail.from(classroom);
    }

    @Transactional
    public ClassroomDetail addExams(Long instituteId, Long id, Set<Long> examIds) {
        Classroom classroom = find(instituteId, id);
        List<Exam> exams = examRepository.findAllById(examIds);
        boolean valid = exams.size() == examIds.size()
                && exams.stream().allMatch(e -> e.getInstitute().getId().equals(instituteId));
        if (!valid) {
            throw new BadRequestException("One or more exams were not found in your institute");
        }
        if (exams.stream().anyMatch(e -> e.getStatus() != ExamStatus.PUBLISHED)) {
            throw new BadRequestException("Only PUBLISHED exams can be assigned to a classroom");
        }
        classroom.getExams().addAll(exams);
        return ClassroomDetail.from(classroom);
    }

    @Transactional
    public ClassroomDetail removeExam(Long instituteId, Long id, Long examId) {
        Classroom classroom = find(instituteId, id);
        classroom.getExams().removeIf(e -> e.getId().equals(examId));
        return ClassroomDetail.from(classroom);
    }

    @Transactional
    public void delete(Long instituteId, Long id) {
        find(instituteId, id).setActive(false);
    }

    private Classroom find(Long instituteId, Long id) {
        return classroomRepository.findByIdAndInstituteIdAndActiveTrue(id, instituteId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found: " + id));
    }
}
