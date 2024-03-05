package com.example.AlgorithmModuleImpl.impl;

import com.example.AlgorithmModuleImpl.data.ImplementData;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InitSolution {
    private final AlgorithmUltils algorithmUltils;
    public InitSolution(ImplementData implementData, AlgorithmUltils algorithmUltils) {

        this.algorithmUltils = algorithmUltils;
    }

    public int[][][] initSolution() {
        int[][][] initSolution = new int[ImplementData.NUMBER_OF_SUBJECT + 1][ImplementData.TOTAL_EXAM_SLOTS + 1][ImplementData.NUMBER_OF_PROCTOR + 1];

        List<Integer> notScheduleSubject = new ArrayList<>();
        for (int i = 0; i < ImplementData.NUMBER_OF_SUBJECT; i++) {
            notScheduleSubject.add(i + 1);
        }

        // Sắp xếp theo số học sinh phải thi. Môn nào nhiều sinh viên thi thì ưu tiên sắp trước
        notScheduleSubject.sort(Comparator.comparingInt(index ->
                Arrays.stream(ImplementData.STUDENT_SUBJECT_MATRIX).mapToInt(row -> row[(int) index]).sum()).reversed());

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


        // Sắp môn học đặc biệt vào slot trước
        Map<Integer, Integer> subjectSlot = getSpecialSubjectSlot();
        for (Map.Entry<Integer, Integer> entry : subjectSlot.entrySet()) {
            int subject = entry.getKey();
            int slotStartSpecial = entry.getValue();
            String action = arrangedSubjectToSlot(subject, slotStartSpecial, notScheduleSubject, roomConstraintStore, proctorConstraintStore, studentConstraintStore,
                    listRandomSlot, initSolution);
            if(action == null){
                // Sắp không được (Do thứ tự môn, random mình sắp), trả về null để chạy lại
                return null;
            }
            if(action.equals("continue")){
                // Subject này sắp vào slot này không được, continue để random slot khác
                continue;
            }
            if(action.equals("break")){
                // Không thể sắp được (Do đầu vào thiếu room, hoặc proctor)
                break;
            }
        }

        List<Integer> notScheduleSubjectCopy = new ArrayList<>(notScheduleSubject);
        // Sắp các môn còn lại vào
        while (true) {
            notScheduleSubject = List.copyOf(notScheduleSubjectCopy);
            if (notScheduleSubject.isEmpty()) {
                break;
            }
            for (Integer subjectIndex : notScheduleSubject) {
                int slotStart = algorithmUltils.getRandomInListInteger(listRandomSlot);
                String action = arrangedSubjectToSlot(subjectIndex, slotStart, notScheduleSubjectCopy, roomConstraintStore, proctorConstraintStore, studentConstraintStore,
                        listRandomSlot, initSolution);
                if(action == null){
                    return null;
                }
                if(action.equals("continue")){
                    continue;
                }
                if(action.equals("break")){
                    break;
                }
            }
        }


        return initSolution;
    }

    // Lấy ra các môn sắp trước
    public Map<Integer, Integer> getSpecialSubjectSlot() {
        Map<Integer, Integer> subjectSlotMap = new HashMap<>();
        for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
            for (int t = 1; t <= ImplementData.TOTAL_EXAM_SLOTS; t++) {
                if(ImplementData.SUBJECT_SLOT_MATRIX[s][t] == 1){
                    subjectSlotMap.put(s,t);
                    break;
                }
            }
        }
        return subjectSlotMap;
    }


    private boolean isConflictStudent(Set<Integer> studentsCurrentSubject, Set<Integer> studentConstraintStore) {
        Set<Integer> listCopy = new HashSet<>(Set.copyOf(studentConstraintStore));
        return !listCopy.addAll(studentsCurrentSubject);
    }

    public Set<Integer> getStudentsCurrentSubject(Integer subjectIndex) {
        int[][] studentSubjectMatrix = ImplementData.STUDENT_SUBJECT_MATRIX;
        Set<Integer> studentsCurrentSubject = new HashSet<>();
        for (int i = 0; i < ImplementData.NUMBER_OF_STUDENT; i++) {
            if (studentSubjectMatrix[i][subjectIndex] == 1) {
                studentsCurrentSubject.add(i);
            }
        }
        return studentsCurrentSubject;
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

    // Tính số lượng phòng theo số lượng sinh viên thi và subject capacity
    public int getNeededRoomBySubject(int subjectIndex) {
        int neededRoom = 0;
        for (int i = 1; i <= ImplementData.NUMBER_OF_STUDENT; i++) {
            neededRoom += ImplementData.STUDENT_SUBJECT_MATRIX[i][subjectIndex];
        }
        return (int) Math.ceil((double) neededRoom / ImplementData.SUBJECT_CAPACITY_VECTOR[subjectIndex]);
    }

    public boolean isDurationConflict(int slotStart, int subjectIndex) {
        int slotsPerSession = ImplementData.NUMBER_OF_EXAM_SLOTS_PER_DAY / 2;
        int startSession = (int) Math.ceil((double) slotStart / slotsPerSession);
        int endSession = (int) Math.ceil((double) (slotStart + ImplementData.SUBJECT_DURATION_VECTOR[subjectIndex] - 1) / slotsPerSession);
        return !(startSession == endSession);
    }

    public String arrangedSubjectToSlot(Integer subjectIndex, int slotStart, List<Integer> notScheduleSubjectCopy, List<Integer> roomConstraintStore,
                                        List<List<Integer>> proctorConstraintStore, List<Set<Integer>> studentConstraintStore, List<Integer> listRandomSlot,
                                        int [][][] initSolution){
        int neededRoom = getNeededRoomBySubject(subjectIndex);
        // Lấy ra subject duration
        int duration = ImplementData.SUBJECT_DURATION_VECTOR[subjectIndex];
        // Check subject xếp quá số lượng slot
        if (slotStart + duration - 1 > ImplementData.TOTAL_EXAM_SLOTS) {
            return "continue";
        }

        // Check số lượng phòng thi vượt quá tổng số lượng phòng
        if (neededRoom > ImplementData.NUMBER_OF_ROOM) {
            notScheduleSubjectCopy.remove(subjectIndex);
            return "break";
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
            return "continue";
        }

        // Check duration cua môn đó
        if (duration > 1) {
            // Check không được sắp vào slot cuối buổi
            if (isDurationConflict(slotStart, subjectIndex)) {
                return "continue";
            }
        }

        // Lấy ra danh sách giám thị có thể trông môn đó
        List<Integer> scheduleProctor = getScheduleProctor(subjectIndex, slotStart, duration, proctorConstraintStore);
        // Check thiếu giám thị cho môn đó, có thể lưu môn đó vào đây trong phần mềm để trả về cho khảo thí
        if (scheduleProctor == null) {
            notScheduleSubjectCopy.remove(subjectIndex);
            return "break";
        }
        if (scheduleProctor.size() < neededRoom) {
            // Check nếu không thể đủ giám thị ở bất cứ slot nào cho môn đó ở đây
            int checkNumberProctor = 0;
            for (int t = 1; t <= ImplementData.TOTAL_EXAM_SLOTS - duration ; t++) {
                if(getScheduleProctor(subjectIndex, t, duration, proctorConstraintStore).size() >= neededRoom){
                    checkNumberProctor++;
                }
            }
            if(checkNumberProctor == 0){
                // Nếu không thể xếp vào được slot nào thì sắp lại
                return null;
            }
            return "continue";
        }
        // Random lại scheduleProctor
        Collections.shuffle(scheduleProctor);

        // Danh sách sinh viên thi môn hiện tại
        Set<Integer> studentsCurrentSubject = getStudentsCurrentSubject(subjectIndex);
        //
        // Check sinh viên không được thi 2 môn trong cùng 1 slot
        if (isConflictStudent(studentsCurrentSubject, studentConstraintStore.get(slotStart))) {
            return "continue";
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
            }
        }
        notScheduleSubjectCopy.remove(subjectIndex);
        return "Success";
    }

}
