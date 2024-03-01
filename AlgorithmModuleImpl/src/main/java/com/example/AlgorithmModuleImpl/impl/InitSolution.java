package com.example.AlgorithmModuleImpl.impl;

import com.example.AlgorithmModuleImpl.data.ImplementData;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InitSolution {
    public InitSolution(ImplementData implementData) {

    }

    public int[][][] initSolution() {
        int[][][] initSolution = new int[ImplementData.NUMBER_OF_SUBJECT + 1][ImplementData.TOTAL_EXAM_SLOTS + 1][ImplementData.NUMBER_OF_PROCTOR + 1];
        List<Integer> notScheduleSubject = new ArrayList<>();
        for (int i = 0; i < ImplementData.NUMBER_OF_SUBJECT; i++) {
            notScheduleSubject.add(i + 1);
        }
        List<Integer> listRandomSlot = new ArrayList<>();
        for (int i = 0; i < ImplementData.TOTAL_EXAM_SLOTS; i++) {
            listRandomSlot.add(i + 1);
        }
        // Danh sách số lượng phòng bận đến khi đến slot của slotConstraintStore
        List<Integer> roomConstraintStore = new ArrayList<>(Collections.nCopies(ImplementData.TOTAL_EXAM_SLOTS + 1, ImplementData.NUMBER_OF_ROOM));
        // Danh sách giám thị đang trông thi
        List<List<Integer>> proctorConstraintStore = new ArrayList<>(Collections.nCopies(ImplementData.TOTAL_EXAM_SLOTS + 1, new ArrayList<>()));
        // Danh sách sinh viên đang thi
        List<Set<Integer>> studentConstraintStore = new ArrayList<>(Collections.nCopies(ImplementData.TOTAL_EXAM_SLOTS + 1, new HashSet<>()));
        List<Integer> notScheduleSubjectCopy = new ArrayList<>(notScheduleSubject);
        int[][] subjectSlotMatrix = ImplementData.SUBJECT_SLOT_MATRIX;
        while (true) {
            notScheduleSubject = List.copyOf(notScheduleSubjectCopy);
            if (notScheduleSubject.isEmpty()) {
                break;
            }
            for (Integer subjectIndex : notScheduleSubject) {
                int slotStart = getRandomInListInteger(listRandomSlot);
                // Tính số lượng phòng theo số lượng sinh viên thi và subject capacity
                int neededRoom = getNeededRoomBySubject(subjectIndex);
                // Lấy ra subject duration
                int duration = ImplementData.SUBJECT_DURATION_VECTOR[subjectIndex];
                // Check subject xếp quá số lượng slot
                if (slotStart + duration - 1 > ImplementData.TOTAL_EXAM_SLOTS) {
                    continue;
                }

                // Check số lượng phòng thi vượt quá tổng số lượng phòng
                if (neededRoom > ImplementData.NUMBER_OF_ROOM) {
                    notScheduleSubjectCopy.remove(subjectIndex);
                    break;
                }

                // Check số lượng phòng thi vượt quá tất cả các slot, không thể xếp vào được slot nào
                int count = 0;
                for (int i = 1; i < roomConstraintStore.size()-duration; i++){
                    boolean outOfAllRoom = false;
                    for (int t = i; t < i + duration; t++) {
                        int availableRoom = roomConstraintStore.get(t);
                        if (neededRoom > availableRoom) {
                            outOfAllRoom = true;
                        }
                    }
                    if(!outOfAllRoom){
                        count++;
                    }
                }
                if(count == 0){
                    return null;
                }

                // Check số lượng phòng thi có đáp ứng được không
                boolean fullRoom = false;
                for (int t = slotStart; t < slotStart + duration; t++) {
                    int availableRoom = roomConstraintStore.get(t);
                    if (neededRoom > availableRoom) {
                        fullRoom = true;
                        break;
                    }
                }
                if (fullRoom) {
                    continue;
                }

                // Check duration cua môn đó
                if (duration > 1) {
                    // Check không được sắp vào slot cuối buổi
                    if (isDurationConflict(slotStart, subjectIndex)) {
                        continue;
                    }
                }

                // Lấy ra danh sách giám thị có thể trông môn đó
                List<Integer> scheduleProctor = getScheduleProctor(subjectIndex, slotStart, duration, proctorConstraintStore);
                // Check thiếu giám thị cho môn đó
                if (scheduleProctor == null) {
                    notScheduleSubjectCopy.remove(subjectIndex);
                    break;
                }
                if (scheduleProctor.size() < neededRoom) {
                    continue;
                }
                // Random lại scheduleProctor

                Collections.shuffle(scheduleProctor);

                // Danh sách sinh viên thi môn hiện tại
                Set<Integer> studentsCurrentSubject = getStudentsCurrentSubject(subjectIndex);
                //
                // Check sinh viên không được thi 2 môn trong cùng 1 slot
                if (isConflictStudent(studentsCurrentSubject, studentConstraintStore.get(slotStart))) {
                    continue;
                }
                // Qua được tất cả constraint => Sắp vào lịch thi
                // Sắp giám thị bằng với số phòng cần thiết
                List<Integer> busyProctor = new ArrayList<>();
                for (int i = 0; i < neededRoom; i++) {
                    // Sắp cho tất cả slot đó thi, đảm bảo vẫn giám thị đó trông môn thi đó
                    for (int j = slotStart; j < slotStart + duration; j++) {
                        initSolution[subjectIndex][j][scheduleProctor.get(i)] = 1;
                    }
                    busyProctor.add(scheduleProctor.get(i));
                }
                // Set lại constraint store
                for (int t = slotStart; t < slotStart + duration; t++) {
                    // constraint proctor lưu những proctor đang bận
                    List<Integer> proctorBusyList = new ArrayList<>(proctorConstraintStore.get(t));
                    proctorBusyList.addAll(busyProctor);
                    proctorConstraintStore.set(t, proctorBusyList);
                    // room constraint lưu lại số lượng phòng còn lại
                    roomConstraintStore.set(t, roomConstraintStore.get(t) - neededRoom);
                    // student constraint lưu lại sinh viên đang bận
                    Set<Integer> studentBusy = new HashSet<>(studentConstraintStore.get(t));
                    studentBusy.addAll(studentsCurrentSubject);
                    studentConstraintStore.set(t, studentBusy);
                    // Nếu không còn phòng hoặc giám thị để xếp thì xóa khỏi slot đó khỏi list random và làm sạch constraint
                    if (proctorConstraintStore.get(t).size() == ImplementData.NUMBER_OF_PROCTOR
                            || roomConstraintStore.get(t) == 0) {
                        listRandomSlot.remove(Integer.valueOf(t));
                        proctorConstraintStore.set(t, new ArrayList<>());
                        studentConstraintStore.set(t, new HashSet<>());
                    }
                }
                notScheduleSubjectCopy.remove(subjectIndex);
            }
        }
        double payoffP0 = payoffP0(initSolution);
        double payoffAllLi = payoffAllLi(initSolution);
        int[] subjectSlotStart = slotStartBySubjectMatrix(initSolution);
        double payoffAllPi = payoffAllPi(subjectSlotStart);
        checkHardConstraint(initSolution, subjectSlotStart);

        return initSolution;
    }

    private int getRandomInListInteger(List<Integer> listRandomSlot) {
        // Sử dụng Random để tạo chỉ mục ngẫu nhiên
        Random random = new Random();
        int randomIndex = random.nextInt(listRandomSlot.size());

        // Trả về phần tử tại chỉ mục ngẫu nhiên
        return listRandomSlot.get(randomIndex);
    }

    private boolean isConflictStudent(Set<Integer> studentsCurrentSubject, Set<Integer> studentConstraintStore) {
        Set<Integer> listCopy = new HashSet<>(Set.copyOf(studentConstraintStore));
        return !listCopy.addAll(studentsCurrentSubject);
    }

    private Set<Integer> getStudentsCurrentSubject(Integer subjectIndex) {
        int[][] studentSubjectMatrix = ImplementData.STUDENT_SUBJECT_MATRIX;
        Set<Integer> studentsCurrentSubject = new HashSet<>();
        for (int i = 0; i < ImplementData.NUMBER_OF_STUDENT; i++) {
            if (studentSubjectMatrix[i][subjectIndex] == 1) {
                studentsCurrentSubject.add(i);
            }
        }
        return studentsCurrentSubject;
    }

    private void updateConstraintStore(int slotStart, List<Integer> slotConstraintStore, List<List<Integer>> proctorConstraintStore, List<Integer> roomConstraintStore, List<Set<Integer>> studentConstraintStore) {
        List<Integer> indices = new ArrayList<>();

        // Tìm các chỉ số của target trong slotConstraintStore
        for (int i = 0; i < slotConstraintStore.size(); i++) {
            if (slotConstraintStore.get(i) == slotStart) {
                indices.add(i);
            }
        }


        // Duyệt qua các chỉ số và cập nhật các ràng buộc
        for (int i = indices.size() - 1; i >= 0; i--) {
            // Đánh dấu giáo viên và lớp đã rảnh
            proctorConstraintStore.remove(indices.get(i).intValue());
            roomConstraintStore.remove(indices.get(i).intValue());
            // Xóa slot đã được sử dụng
            slotConstraintStore.remove(indices.get(i).intValue());
            // Xóa danh sách sinh viên
            studentConstraintStore.remove(indices.get(i).intValue());
        }
    }

    private List<Integer> getScheduleProctor(int subjectIndex, int slotStart, int duration, List<List<Integer>> proctorConstraintStore) {
        List<Integer> proctorsBySubject = new ArrayList<>();
        for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
            if (ImplementData.PROCTOR_SUBJECT_MATRIX[g][subjectIndex] == 1) {
                proctorsBySubject.add(g);
            }
        }
        // Check không đủ giám thị để trông
        if (proctorsBySubject.size() < getNeededRoomBySubject(subjectIndex)) {
            return null;
        }
        List<Integer> scheduleProctor = new ArrayList<>(proctorsBySubject);

        for (Integer proctor : proctorsBySubject) {
            for (int t = slotStart; t < slotStart + duration; t++) {
                if (proctorConstraintStore.get(t).contains(proctor)) {
                    scheduleProctor.remove(proctor);
                }
            }
        }
        return scheduleProctor;
    }

    private List<Integer> specificProctorSubject(int subjectIndex) {
        List<Integer> specificProctors = new ArrayList<>();
        int[][] proctorSubjectMatrix = ImplementData.PROCTOR_SUBJECT_MATRIX;
        for (int i = 0; i < proctorSubjectMatrix.length; i++) {
            if (proctorSubjectMatrix[i][subjectIndex] == 1) {
                specificProctors.add(i);
            }
        }
        return specificProctors;
    }

    // Tính số lượng phòng theo số lượng sinh viên thi và subject capacity
    private int getNeededRoomBySubject(int subjectIndex) {
        int neededRoom = 0;
        for (int i = 1; i <= ImplementData.NUMBER_OF_STUDENT; i++) {
            neededRoom += ImplementData.STUDENT_SUBJECT_MATRIX[i][subjectIndex];
        }
        return (int) Math.ceil((double) neededRoom / ImplementData.SUBJECT_CAPACITY_VECTOR[subjectIndex]);
    }

    private boolean isDurationConflict(int slotStart, int subjectIndex) {
        int slotsPerSession = ImplementData.NUMBER_OF_EXAM_SLOTS_PER_DAY / 2;
        int startSession = (int) Math.ceil((double) slotStart / slotsPerSession);
        int endSession = (int) Math.ceil((double) (slotStart + ImplementData.SUBJECT_DURATION_VECTOR[subjectIndex] - 1) / slotsPerSession);
        return !(startSession == endSession);
    }

    // Lấy ra ma trận Ts => slot bắt đầu thi của môn s
    private int[] slotStartBySubjectMatrix(int[][][] solution) {
        int[] subjectSlotVector = new int[ImplementData.NUMBER_OF_SUBJECT + 1];
        for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
            boolean slotStart = false;
            for (int t = 1; t <= ImplementData.TOTAL_EXAM_SLOTS; t++) {
                if (slotStart) {
                    break;
                }
                for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                    if (solution[s][t][g] == 1) {
                        slotStart = true;
                        subjectSlotVector[s] = t;
                        break;
                    }
                }
            }
        }
        return subjectSlotVector;
    }

    //Hàm tính payoff của P0: Số lượng giám thị đồng đều giữa các ca thi
    private double payoffP0(int[][][] solution) {
        double averageProctorEachSlot = 0;
        double payoffValue = 0;
        // Tìm trung bình mỗi slot tối ưu sẽ có bao nhiêu giám thị
        for (int t = 1; t <= ImplementData.TOTAL_EXAM_SLOTS; t++) {
            for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
                for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                    averageProctorEachSlot += solution[s][t][g];
                }
            }
        }
        averageProctorEachSlot = averageProctorEachSlot / ImplementData.TOTAL_EXAM_SLOTS;
        for (int t = 1; t <= ImplementData.TOTAL_EXAM_SLOTS; t++) {
            double proctorEachSlot = 0;
            for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
                for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                    proctorEachSlot += solution[s][t][g];
                }
            }
            payoffValue += Math.pow(proctorEachSlot - averageProctorEachSlot, 2);
        }
        return payoffValue;
    }

    // TO-DO: Hàm tính payoff của All Pi (Sinh viên)
    // Các môn của mối sinh viên được dàn trải đều
    private double payoffAllPi(int[] subjectSlotStart) {
        double payoffAllPi = 0;
        for (int m = 1; m <= ImplementData.NUMBER_OF_STUDENT; m++) {
            double payoffPi = 0;
            List<Integer> examSlotEachStudent = new ArrayList<>();
            for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
                if (ImplementData.STUDENT_SUBJECT_MATRIX[m][s] == 1) {
                    // Trong trường hợp có một vài môn không đủ điều kiện để xếp
                    if (subjectSlotStart[s] != 0) {
                        examSlotEachStudent.add(subjectSlotStart[s]);
                    }
                }
            }
            examSlotEachStudent.sort(Comparator.reverseOrder());
            for (int i = 0; i < examSlotEachStudent.size() - 1; i++) {
                payoffPi += Math.pow(examSlotEachStudent.get(i) - examSlotEachStudent.get(i + 1) - (ImplementData.TOTAL_EXAM_SLOTS / examSlotEachStudent.size()), 2);
            }
            payoffAllPi += payoffPi;
        }
        return payoffAllPi;
    }

    // TO-DO: Hàm tính payoff của All Li (Giám thị)
    private double payoffAllLi(int[][][] solution) {
        // Giám thị có lịch trông thi phải lên ít ngày nhất
        double payoffAllLi = 0;
        for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
            int proctorSuperviseSlot = 0;
            int proctorSuperviseDay = 0;
            for (int d = 1; d <= ImplementData.NUMBER_OF_EXAM_DAYS; d++) {
                int proctorSuperviseSlotPerDay = 0;
                for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
                    for (int t = (d - 1) * ImplementData.NUMBER_OF_EXAM_SLOTS_PER_DAY + 1; t <= d * ImplementData.NUMBER_OF_EXAM_SLOTS_PER_DAY; t++) {
                        proctorSuperviseSlotPerDay += solution[s][t][g];
                    }
                }
                // Số ngày giám thị lên trông thi
                if (proctorSuperviseSlotPerDay > 0) {
                    proctorSuperviseSlot += proctorSuperviseSlotPerDay;
                    proctorSuperviseDay++;
                }
            }
            // PayoffAllPi
            payoffAllLi += 0.5 * proctorSuperviseDay + 0.5 * Math.abs(proctorSuperviseSlot - ImplementData.PROCTOR_QUOTA_VECTOR[g]);
        }


        return payoffAllLi;
    }
    // TO-DO: Hàm tính fitness function

    // Hàm check hard constraint
    private void checkHardConstraint(int[][][] solution, int[] subjectSlotStart) {
        // H1: Tất cả các môn đều được sắp lịch thi
        int numberOfViolate = 0;
        for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
            // Số giám thị được phân thực tế
            double numberProctorEachSubject = 0;
            for (int t = 1; t <= ImplementData.TOTAL_EXAM_SLOTS; t++) {
                for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                    numberProctorEachSubject += solution[s][t][g];
                }
            }
            // Số phòng cần tổ chức thi của môn này
            double neededRoom = getNeededRoomBySubject(s);
            // Số giám thị cần phải phân theo kế hoạch
            double neededProctor = ImplementData.SUBJECT_DURATION_VECTOR[s] * neededRoom;
            if (numberProctorEachSubject != neededProctor) {
                numberOfViolate++;
            }
        }

        //H2: Trong cùng một ca sinh viên không được thi 2 môn khác nhau TO-DO: Cần tối ưu lại hàm dưới, check quá lâu
        for (int t = 1; t <= ImplementData.TOTAL_EXAM_SLOTS; t++) {
            Set<Integer> studentEachSlot = new HashSet<>();
            for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
                for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                    if (solution[s][t][g] == 1) {
                        if (!studentEachSlot.addAll(getStudentsCurrentSubject(s))) {
                            numberOfViolate++;
                        }
                        break;
                    }
                }
            }
        }


        //H3: Số lượng phòng thi không được vượt quá số lượng cho phép
        for (int t = 1; t <= ImplementData.TOTAL_EXAM_SLOTS; t++) {
            int numberOfRoom = 0;
            for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
                for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                    numberOfRoom += solution[s][t][g];
                }
            }
            if (numberOfRoom > ImplementData.NUMBER_OF_ROOM) {
                numberOfViolate++;
            }
        }

        // H4: Với mỗi môn thi, giám thị được phân công cần trông hết cả các slot liên tiếp mà môn thi đó diễn ra
        // Phải lấy ra được Ts
        for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
            for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                for (int t = subjectSlotStart[s]; t <= subjectSlotStart[s] + ImplementData.SUBJECT_DURATION_VECTOR[s] - 2; t++) {
                    if (solution[s][t][g] - solution[s][t + 1][g] != 0) {
                        numberOfViolate++;
                    }
                }
            }
        }
        System.out.println();


        // H5: Số giám thị xếp cho mỗi môn phải bằng số phòng của môn đó tại mỗi slot
        for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
            for (int t = subjectSlotStart[s]; t <= subjectSlotStart[s] + ImplementData.SUBJECT_DURATION_VECTOR[s] - 1; t++) {
                int numberOfProctor = 0;
                for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                    numberOfProctor += solution[s][t][g];
                }
                int neededRoom = getNeededRoomBySubject(s);
                if (numberOfProctor != neededRoom) {
                    numberOfViolate++;
                }
            }
        }

        //H6: Trong cùng một ca giám thị chỉ được trông một môn
        for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
            for (int t = 0; t < ImplementData.TOTAL_EXAM_SLOTS; t++) {
                // Số lượng môn giám thị đó phải trông
                int subjectEachSlot = 0;
                for (int s = 0; s < ImplementData.NUMBER_OF_SUBJECT; s++) {
                    subjectEachSlot += solution[s][t][g];
                }
                if (subjectEachSlot > 1) {
                    numberOfViolate++;
                }
            }
        }

        // H7: Giám thị chỉ được phân môn họ có thể trông được, matrix proctor_subject đang không đúng, phải sửa lại
        for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
            for (int t = 1; t <= ImplementData.TOTAL_EXAM_SLOTS; t++) {
                for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                    if (solution[s][t][g] > ImplementData.PROCTOR_SUBJECT_MATRIX[g][s]) {
                        numberOfViolate++;
                    }
                }
            }
        }
        // H8: Các môn có duration > 1 cần sắp sao cho môn đó phải tổ chức trong cùng một buổi
        for (int s = 1; s < ImplementData.NUMBER_OF_SUBJECT; s++) {
            if (isDurationConflict(subjectSlotStart[s], s)) {
                numberOfViolate++;
            }
        }
        // TO-DO H9, H10: Để làm sau
        System.out.println();
    }

//    private List<int[][][]> generateNeighbors(int[][][] initSolution, int[] subjectSlotStart) {
//        for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
//            int slotStart = subjectSlotStart[s];
//            for (int t = slotStart; t <= ImplementData.TOTAL_EXAM_SLOTS; t++) {
//
//            }
//
//        }
//    }
}
