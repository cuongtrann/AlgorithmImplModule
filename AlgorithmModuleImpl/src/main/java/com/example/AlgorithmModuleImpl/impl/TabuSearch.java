package com.example.AlgorithmModuleImpl.impl;

import com.example.AlgorithmModuleImpl.data.ImplementData;
import jdk.jshell.spi.ExecutionControl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TabuSearch {

    public TabuSearch(ImplementData implementData) {
    }

    public int [][][] initSolution(){
        int [][][] initSolution = new int [ImplementData.NUMBER_OF_SUBJECT + 1][ImplementData.TOTAL_EXAM_SLOTS + 1][ImplementData.NUMBER_OF_PROCTOR + 1];
        List<Integer> notScheduleSubject = new ArrayList<>();
        List<Integer> notScheduleProctor = new ArrayList<>();
        for (int i = 0; i < ImplementData.NUMBER_OF_SUBJECT; i++) {
            notScheduleSubject.add(i+1);
        }
        for (int i = 0; i < ImplementData.NUMBER_OF_PROCTOR; i++) {
            notScheduleProctor.add(i+1);
        }
        // Số lượng phòng có thể sử dụng ban đầu của mỗi slot
        int availableRoom = ImplementData.NUMBER_OF_ROOM;
        // List các môn học đã được sắp lịch
        List<Integer> scheduledSubject = new ArrayList<>();
        // Số lượng phòng phải bị trừ đi trong slot tiếp theo (Trường hợp subject duration > 1)
        int unAvailableRoom = 0;
        // Danh sách giám thị phải bị trừ đi trong slot tiếp theo(Trường hợp subject duration > 1)
        List<Integer> unAvailableProctor = new ArrayList<>();

        // Duyệt qua từng slot
        for (int i = 1; i <= ImplementData.TOTAL_EXAM_SLOTS; i++) {
            // TO-DO: Cap nhat so luong phong thi cho slot tiep theo
            // TO-DO: Cap nhat danh sach giam thi cho slot tiep theo
            // TO-DO: Cap nhat danh sach mon hoc con lai can phai sap xep
            // Duyệt theo môn
            for (int subjectIndex: notScheduleSubject){
                boolean isOneSlot = true;
                //Check available room = 0 => slot tiếp theo
                if(availableRoom == 0){
                    break;
                }
                // Tính số lượng phòng theo số lượng sinh viên thi và subject capacity
                int neededRoom = getNeededRoomBySubject(subjectIndex);
                // Check số lượng phòng thi có đáp ứng được không
                if(neededRoom > availableRoom){
                    continue;
                }
                //
                // Check duration cua môn đó
                if(ImplementData.SUBJECT_DURATION_VECTOR[subjectIndex] > 1){
                    isOneSlot = false;
                    // Check không được sắp vào slot cuối buổi
                    if(isDurationConflict(i,subjectIndex)){
                        continue;
                    }
                }
                //
                // Check sinh viên không được thi 2 môn trong cùng 1 slot
                if(isConflictStudent(initSolution, i)){
                    continue;
                }
                //
                //TO-DO: Check có phải môn đặc biệt không, nếu có thì lấy ra list proctor trông được môn đó

                //


                // TO-DO: Cập nhật số lượng phòng thi để sắp môn tiếp theo
                availableRoom = availableRoom - neededRoom;
                // TO-DO: Cập nhật danh sách giám thị để sắp môn tiếp theo
                System.out.println();
            }
        }
        return initSolution;
    }
    // Tính số lượng phòng theo số lượng sinh viên thi và subject capacity
    private int getNeededRoomBySubject(int subjectIndex) {
        int neededRoom = 0;
        for (int i = 1; i <= ImplementData.NUMBER_OF_STUDENT; i++) {
            neededRoom += ImplementData.STUDENT_SUBJECT_MATRIX[i][subjectIndex];
        }
        return (int) Math.ceil ((double) neededRoom / ImplementData.SUBJECT_CAPACITY_VECTOR[subjectIndex]);
    }

    private boolean isConflictStudent(int[][][] initSolution, int currentSLot){
        int sum = 0;
        for (int m = 1; m <= ImplementData.NUMBER_OF_STUDENT; m++) {
            for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
                for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                    sum += initSolution[s][currentSLot][g] * ImplementData.STUDENT_SUBJECT_MATRIX[m][s];
                }
            }
        }
        return sum > 0;
    }

    private boolean isDurationConflict(int slotStart,  int subjectIndex){
        int slotsPerSession = ImplementData.NUMBER_OF_EXAM_SLOTS_PER_DAY /2;
        int startSession= (int) Math.ceil((double) slotStart / slotsPerSession);
        int endSession = (int) Math.ceil((double) (slotStart + ImplementData.SUBJECT_DURATION_VECTOR[subjectIndex] - 1) / slotsPerSession);
        return !(startSession == endSession);
    }
}
