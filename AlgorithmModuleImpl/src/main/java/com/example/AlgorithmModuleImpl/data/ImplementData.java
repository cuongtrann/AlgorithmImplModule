package com.example.AlgorithmModuleImpl.data;

import com.example.AlgorithmModuleImpl.model.Proctor;
import com.example.AlgorithmModuleImpl.model.Room;
import com.example.AlgorithmModuleImpl.model.Student;
import com.example.AlgorithmModuleImpl.model.Subject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Service
public class ImplementData {
    public static int NUMBER_OF_EXAM_DAYS = 7;
    public static int NUMBER_OF_EXAM_SLOTS_PER_DAY = 6;
    public static int TOTAL_EXAM_SLOTS = NUMBER_OF_EXAM_SLOTS_PER_DAY * NUMBER_OF_EXAM_DAYS;
    public static int NUMBER_OF_ROOM;
    public static int NUMBER_OF_SUBJECT;
    public static int NUMBER_OF_PROCTOR;
    public static int NUMBER_OF_STUDENT;
    // Danh sách tất cả subject
    public static List<Subject> GENERAL_SUBJECT_LIST = new ArrayList<>();
    public static List<Proctor> PROCTOR_LIST = new ArrayList<>();
    public static List<Student> STUDENT_LIST = new ArrayList<>();
    public static List<Room> ROOM_LIST = new ArrayList<>();

    // Danh sách các subject cần tổ chức thi
//    public static List<Subject> EXAM_SUBJECT_LIST = new ArrayList<>();
    public static int[] SUBJECT_CAPACITY_VECTOR = new int[0];
    public static int[] SUBJECT_DURATION_VECTOR = new int[0];
    public static int[] PROCTOR_QUOTA_VECTOR = new int[0];

    public static int[][] STUDENT_SUBJECT_MATRIX = new int[0][0];

    public static int[][] PROCTOR_SUBJECT_MATRIX = new int[0][0];

    public static int[][] SUBJECT_SLOT_MATRIX = new int[0][0];

    public static List<Integer> SUBJECT_INDEX_LIST = new ArrayList<>();


    public ImplementData() {
        // Đọc file và khởi tạo data ở đây
        try {
            // Đường dẫn đến file Excel
            String excelFilePath = "D:\\\\FPT_University\\\\Capstone_Project\\\\TestData\\\\TestingData.xlsx";

            // Tạo một FileInputStream để đọc file Excel
            FileInputStream inputStream = new FileInputStream(excelFilePath);

            // Tạo một workbook từ FileInputStream
            Workbook workbook = new XSSFWorkbook(inputStream);

            // Lấy sheet đầu tiên từ workbook
            getSubjectData(workbook);
            getStudentEnrolment(workbook);
            getRoomData(workbook);
            getProctorData(workbook);
            getProctorSubjectData(workbook);
            getSubjectSlotData(workbook);
            // Đóng workbook và FileInputStream sau khi sử dụng
            workbook.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getSubjectData(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0);
        int index = 1;
        // Duyệt qua các dòng của sheet
        for (Row row : sheet) {
            // Duyệt qua các ô của mỗi dòng
            Subject subject = new Subject();
            subject.setName(row.getCell(0).toString());
            subject.setDuration((int) row.getCell(1).getNumericCellValue());
            subject.setCapacity((int) row.getCell(2).getNumericCellValue());
            subject.setIndex(index);
            GENERAL_SUBJECT_LIST.add(subject);
            //
            SUBJECT_INDEX_LIST.add(index);
            index++;
        }
        NUMBER_OF_SUBJECT = GENERAL_SUBJECT_LIST.size();
        SUBJECT_DURATION_VECTOR = new int[NUMBER_OF_SUBJECT + 1];
        SUBJECT_CAPACITY_VECTOR = new int[NUMBER_OF_SUBJECT + 1];
        // Vector B - bs => duration
        for (int i = 0; i < GENERAL_SUBJECT_LIST.size(); i++) {
            int durationBySlot = GENERAL_SUBJECT_LIST.get(i).getDuration();
            int capacity = GENERAL_SUBJECT_LIST.get(i).getCapacity();
            if (durationBySlot <= 90) {
                durationBySlot = 1;
            } else if (durationBySlot <= 180) {
                durationBySlot = 2;
            } else {
                durationBySlot = 3;
            }
            SUBJECT_DURATION_VECTOR[i + 1] = durationBySlot;
            SUBJECT_CAPACITY_VECTOR[i + 1] = capacity;
        }
    }

    private void getSubjectSlotData(Workbook workbook) {
        // Matrix H hgt: if s must be exam in slot t
        Sheet sheet = workbook.getSheetAt(5);
        // Duyệt qua các dòng của sheet
        SUBJECT_SLOT_MATRIX = new int[NUMBER_OF_SUBJECT+1][TOTAL_EXAM_SLOTS+1];
        for (Row row : sheet) {
            // Duyệt qua các ô của mỗi dòng
            String subjectCode = row.getCell(0).toString();
            Optional<Subject> foundSubject = GENERAL_SUBJECT_LIST.stream()
                    .filter(subject -> subject.getName().equals(subjectCode))
                    .findFirst();
            if (foundSubject.isPresent()) {
                SUBJECT_SLOT_MATRIX[foundSubject.get().getIndex()][(int) row.getCell(1).getNumericCellValue()] = 1;
            }
        }

    }

    private void getProctorSubjectData(Workbook workbook) {
        // Matrix E egs: if g can exam s
        Sheet sheet = workbook.getSheetAt(4);
        // Duyệt qua các dòng của sheet
        PROCTOR_SUBJECT_MATRIX = new int[NUMBER_OF_PROCTOR + 1][NUMBER_OF_SUBJECT + 1];
        Set<Integer> specificSubject = new HashSet<>();
        for (Row row : sheet) {
            // Duyệt qua các ô của mỗi dòng
            String subjectCode = row.getCell(0).toString();
            String proctorName = row.getCell(1).toString();
            Optional<Proctor> foundProctor = PROCTOR_LIST.stream()
                    .filter(proctor -> proctor.getName().equals(proctorName))
                    .findFirst();
            Optional<Subject> foundSubject = GENERAL_SUBJECT_LIST.stream()
                    .filter(subject -> subject.getName().equals(subjectCode))
                    .findFirst();
            if (foundProctor.isPresent() && foundSubject.isPresent()) {
                PROCTOR_SUBJECT_MATRIX[foundProctor.get().getIndex()][foundSubject.get().getIndex()] = 1;
                specificSubject.add(foundSubject.get().getIndex());
            }
        }
        for (int s = 1; s <= NUMBER_OF_SUBJECT ; s++) {
            if(specificSubject.add(s)){
                for (int g = 1; g <= NUMBER_OF_PROCTOR ; g++) {
                    PROCTOR_SUBJECT_MATRIX[g][s] = 1;
                }
            }
        }

    }

    private void getProctorData(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(3);
        int index = 1;
        // Duyệt qua các dòng của sheet
        for (Row row : sheet) {
            // Duyệt qua các ô của mỗi dòng
            Proctor proctor = new Proctor();
            proctor.setName(row.getCell(0).toString());
            proctor.setQuota((int) row.getCell(1).getNumericCellValue());

            proctor.setIndex(index);
            PROCTOR_LIST.add(proctor);
            index++;
        }
        NUMBER_OF_PROCTOR = PROCTOR_LIST.size();
        // Vector C - cg => quota
        PROCTOR_QUOTA_VECTOR = new int[NUMBER_OF_PROCTOR + 1];
        for (Proctor proctor : PROCTOR_LIST) {
            PROCTOR_QUOTA_VECTOR[proctor.getIndex()] = (proctor.getQuota());
        }
    }

    private void getRoomData(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(2);
        int index = 1;
        // Duyệt qua các dòng của sheet
        for (Row row : sheet) {
            // Duyệt qua các ô của mỗi dòng
            Room room = new Room();
            room.setName(row.getCell(0).toString());
            room.setIndex(index);
            ROOM_LIST.add(room);
            index++;
        }
        NUMBER_OF_ROOM = ROOM_LIST.size();
    }

    private void getStudentEnrolment(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(1);
        int index = 1;
        // Duyệt qua các dòng của sheet
        List<String> existStudentRollNumber = new ArrayList<>();
        for (Row row : sheet) {
            // Lấy ra danh sách sinh viên kì đó
            String rollNumber = row.getCell(0).toString();
            boolean existedRollNumber = false;
            for (String existRollNumber : existStudentRollNumber) {
                if (existRollNumber.equals(rollNumber)) {
                    existedRollNumber = true;
                    break;
                }
            }
            if (!existedRollNumber) {
                existStudentRollNumber.add(rollNumber);
                Student student = new Student();
                student.setRollNumber(rollNumber);
                student.setIndex(index);
                STUDENT_LIST.add(student);
                index++;
            }
        }
        NUMBER_OF_STUDENT = STUDENT_LIST.size();
        STUDENT_SUBJECT_MATRIX = new int[NUMBER_OF_STUDENT + 1][NUMBER_OF_SUBJECT + 1];
        // Luu matrix D - dms: Sinh vien nao thi mon nao
        for (Row row : sheet) {
            // Lấy ra danh sách sinh viên kì đó
            String rollNumber = row.getCell(0).toString();
            String subjectCode = row.getCell(1).toString();
            Optional<Student> foundStudent = STUDENT_LIST.stream()
                    .filter(student -> student.getRollNumber().equals(rollNumber))
                    .findFirst();
            Optional<Subject> foundSubject = GENERAL_SUBJECT_LIST.stream()
                    .filter(subject -> subject.getName().equals(subjectCode))
                    .findFirst();
            if (foundStudent.isPresent() && foundSubject.isPresent()) {
                STUDENT_SUBJECT_MATRIX[foundStudent.get().getIndex()][foundSubject.get().getIndex()] = 1;
            }
        }
    }


}
