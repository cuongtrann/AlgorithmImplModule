package com.example.AlgorithmModuleImpl.impl;

import com.example.AlgorithmModuleImpl.data.ImplementData;
import com.example.AlgorithmModuleImpl.model.Solution;
import org.apache.poi.util.ArrayUtil;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.util.ArrayUtils;

import java.util.*;

@Service
public class TabuSearchImplement {
    private final AlgorithmUltils algorithmUltils;
    private final InitSolution initSolution;

    public TabuSearchImplement(AlgorithmUltils algorithmUltils, InitSolution initSolution) {
        this.algorithmUltils = algorithmUltils;
        this.initSolution = initSolution;
    }

    // Thuật toán Tabu Search
    public int[][][] tabuSearch(int[][][] initialSolution, int maxIterations, int tabuListSize) {
        int[][][] currentSolution = initialSolution;
        int[][][] bestSolution = initialSolution;
        double currentCost = calculateCost(currentSolution);
        double bestCost = currentCost;

        List<int[]> tabuList = new ArrayList<>();

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            Solution neighbor = generateNeighbors2(currentSolution, tabuList, tabuListSize);

            double neighborCost = neighbor.getFitness();
            int [][][] neighborSolution = neighbor.getSolution();

            if (neighborCost < currentCost) {
                currentSolution = neighborSolution;
                currentCost = neighborCost;

                if (currentCost < bestCost) {
                    bestSolution = currentSolution;
                    bestCost = currentCost;
                }
            } else {
                // Điều này có thể cần điều chỉnh tùy thuộc vào chiến lược của bạn
                currentSolution = neighborSolution;
                currentCost = neighborCost;
            }

        }
        double payoffP0 = payoffP0(bestSolution);
        int[] slotStart = slotStartBySubjectMatrix(bestSolution);
        double payoffPi = payoffAllPi(slotStart);
        double payoffLi = payoffAllLi(bestSolution);
        return bestSolution;
    }

    public double calculateCost(int[][][] solution) {
        // Thực hiện tính toán chi phí của giải pháp, dựa vào yêu cầu cụ thể của bạn
        // Trả về chi phí của giải pháp
        int[] subjectSlotStart = slotStartBySubjectMatrix(solution);
        if (!checkHardConstraintBoolean(solution, subjectSlotStart)) {
            return 10000000;
        }
        double payoffP0 = payoffP0(solution);
        double payoffAllLi = payoffAllLi(solution);
        double payoffAllPi = payoffAllPi(subjectSlotStart);
        double fitnessValue = fitnessValue(payoffP0, payoffAllPi, payoffAllLi);

        return fitnessValue;
    }


    // Generate solution bằng cách đổi slot giữa 2 môn liền kề
    private List<int[][][]> generateNeighbors(int[][][] solution) {
        List<int[][][]> neiborList = new ArrayList<>();
        int[] subjectSlotStart = slotStartBySubjectMatrix(solution);
        for (int i = 1; i < subjectSlotStart.length - 1; i++) {
            int[][][] copySolution = copy3DArray(solution);
            int loop = 0;
            boolean outOfSlot = false;
            for (int t = subjectSlotStart[i]; t < subjectSlotStart[i] + ImplementData.SUBJECT_DURATION_VECTOR[i]; t++) {
                if (subjectSlotStart[i + 1] + loop > ImplementData.TOTAL_EXAM_SLOTS) {
                    outOfSlot = true;
                    break;
                }
                int[] temp = copySolution[i][t];
                copySolution[i][t] = copySolution[i][subjectSlotStart[i + 1] + loop];
                copySolution[i][subjectSlotStart[i + 1] + loop] = temp;
                loop++;
            }
            loop = 0;
            for (int t = subjectSlotStart[i + 1]; t < subjectSlotStart[i + 1] + ImplementData.SUBJECT_DURATION_VECTOR[i + 1]; t++) {
                if (subjectSlotStart[i] + loop > ImplementData.TOTAL_EXAM_SLOTS) {
                    outOfSlot = true;
                    break;
                }
                int[] temp = copySolution[i + 1][t];
                copySolution[i + 1][t] = copySolution[i + 1][subjectSlotStart[i] + loop];
                copySolution[i + 1][subjectSlotStart[i] + loop] = temp;
                loop++;
            }
            if (outOfSlot) {
                continue;
            }
            neiborList.add(copySolution);
        }
        return neiborList;
    }

    // Generate solution bằng cách đổi slot giữa 2 môn bất kì
    private List<int[][][]> generateNeighbors1(int[][][] solution) {
        Set<int[][][]> neiborList = new HashSet<>();
        List<Integer> randomListSubject = new ArrayList<>(ImplementData.SUBJECT_INDEX_LIST);
        Map<Integer, Integer> specialSubjectSlot = initSolution.getSpecialSubjectSlot();
        randomListSubject.removeAll(specialSubjectSlot.keySet());
        int[] subjectSlotStart = slotStartBySubjectMatrix(solution);
        while (neiborList.size() < 100) {
            int[][][] copySolution = copy3DArray(solution);
            int loop = 0;
            boolean outOfSlot = false;
            int subject1 = algorithmUltils.getRandomInListInteger(randomListSubject);
            int subject2 = algorithmUltils.getRandomInListInteger(randomListSubject);

            for (int t = subjectSlotStart[subject1]; t < subjectSlotStart[subject1] + ImplementData.SUBJECT_DURATION_VECTOR[subject1]; t++) {
                if (subjectSlotStart[subject2] + loop > ImplementData.TOTAL_EXAM_SLOTS) {
                    outOfSlot = true;
                    break;
                }
                int[] temp = copySolution[subject1][t];
                copySolution[subject1][t] = copySolution[subject1][subjectSlotStart[subject2] + loop];
                copySolution[subject1][subjectSlotStart[subject2] + loop] = temp;
                loop++;
            }
            loop = 0;
            for (int t = subjectSlotStart[subject2]; t < subjectSlotStart[subject2] + ImplementData.SUBJECT_DURATION_VECTOR[subject2]; t++) {
                if (subjectSlotStart[subject1] + loop > ImplementData.TOTAL_EXAM_SLOTS) {
                    outOfSlot = true;
                    break;
                }
                int[] temp = copySolution[subject2][t];
                copySolution[subject2][t] = copySolution[subject2][subjectSlotStart[subject1] + loop];
                copySolution[subject2][subjectSlotStart[subject1] + loop] = temp;
                loop++;
            }
            if (outOfSlot) {
                continue;
            }
            neiborList.add(copySolution);
        }


        return neiborList.stream().toList();
    }

    // Generate solution bằng cách đổi slot với tất cả các slot còn lại
    public Solution generateNeighbors2(int [][][] solution, List<int[]> tabuList, int tabuListSize){
//        List<int[][][]> neiborList = new ArrayList<>();
        int[] subjectSlotStart = slotStartBySubjectMatrix(solution);
        List<Integer> listSubject = new ArrayList<>(ImplementData.SUBJECT_INDEX_LIST);
        Map<Integer, Integer> specialSubjectSlot = initSolution.getSpecialSubjectSlot();
        listSubject.removeAll(specialSubjectSlot.keySet());
        int[][][] bestSolution = null;
        double bestCost = Double.MAX_VALUE;

        for (int i = 0; i < listSubject.size() - 1; i++) {
            for (int j = i+1; j < listSubject.size(); j++) {
                int[][][] copySolution = copy3DArray(solution);
                int loop = 0;
                int subject1 = listSubject.get(i);
                int subject2 = listSubject.get(j);

                // Lấy ra subject slotStart của solution mới để check xem có trong tabuList không
                int [] subjectSlotStartCurrent = Arrays.copyOf(subjectSlotStart, subjectSlotStart.length);
                int tempSlotStart = subjectSlotStartCurrent[subject1];
                subjectSlotStartCurrent[subject1] = subjectSlotStartCurrent[subject2];
                subjectSlotStartCurrent[subject2] = tempSlotStart;
                if(!containsDuplicateArray(tabuList, subjectSlotStartCurrent)){
                    tabuList.add(subjectSlotStartCurrent);
                    updateTabuList(tabuList, tabuListSize);
                }else{
                    // Đã có thì không cần swap nữa
                    continue;
                }

                // Check duration nếu mà bị out of slot thì continue luôn, không chạy xuống dưới
                if(subjectSlotStart[subject2] + ImplementData.SUBJECT_DURATION_VECTOR[subject1] - 1 > ImplementData.TOTAL_EXAM_SLOTS
                || subjectSlotStart[subject1] + ImplementData.SUBJECT_DURATION_VECTOR[subject2] - 1 > ImplementData.TOTAL_EXAM_SLOTS){
                    continue;
                }

                for (int t = subjectSlotStart[subject1]; t < subjectSlotStart[subject1] + ImplementData.SUBJECT_DURATION_VECTOR[subject1]; t++) {
                    int[] temp = copySolution[subject1][t];
                    copySolution[subject1][t] = copySolution[subject1][subjectSlotStart[subject2] + loop];
                    copySolution[subject1][subjectSlotStart[subject2] + loop] = temp;
                    loop++;
                }

                loop = 0;
                for (int t = subjectSlotStart[subject2]; t < subjectSlotStart[subject2] + ImplementData.SUBJECT_DURATION_VECTOR[subject2]; t++) {
                    int[] temp = copySolution[subject2][t];
                    copySolution[subject2][t] = copySolution[subject2][subjectSlotStart[subject1] + loop];
                    copySolution[subject2][subjectSlotStart[subject1] + loop] = temp;
                    loop++;
                }


                // Tìm ra solution có fitness tốt nhất
                double neighborCost = calculateCost(copySolution);
                if (neighborCost < bestCost) {
                    bestCost = neighborCost;
                    bestSolution = copySolution;
                }
            }
        }


        return new Solution(bestSolution, bestCost);
    }

    public boolean containsDuplicateArray(List<int[]> tabuList, int[] slotStartList){
        return tabuList.stream().anyMatch(existingArray -> Arrays.equals(existingArray, slotStartList));
    }






    private int[][][] copy3DArray(int[][][] original) {
        int[][][] destinationArray = new int[original.length][][];

        for (int i = 0; i < original.length; i++) {
            destinationArray[i] = new int[original[i].length][];
            for (int j = 0; j < original[i].length; j++) {
                int[] row = new int[original[i][j].length];
                System.arraycopy(original[i][j], 0, row, 0, original[i][j].length);
                destinationArray[i][j] = row;
            }
        }

        return destinationArray;
    }

    private static boolean isTabu(int[][][] solution, Set<int[][][]> tabuList) {
        // Kiểm tra xem giải pháp có trong danh sách tabu không
        return tabuList.add(solution);
    }

    private static void updateTabuList(List<int[]> tabuList, int tabuListSize) {
        // Cập nhật danh sách tabu, có thể loại bỏ các phần tử cũ
        if (tabuList.size() > tabuListSize) {
            tabuList.remove(0);
        }
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
    public double payoffP0(int[][][] solution) {
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

    // Hàm tính payoff của All Pi (Sinh viên)
    // Các môn của mối sinh viên được dàn trải đều
    public double payoffAllPi(int[] subjectSlotStart) {
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
    public double payoffAllLi(int[][][] solution) {
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
    public double fitnessValue(double payoffP0, double payoffAllPi, double payoffAllLi) {
        return (double) 1 / 3 * (payoffP0 + payoffAllPi + payoffAllLi);
    }

    // Hàm check hard constraint
    private int checkHardConstraint(int[][][] solution, int[] subjectSlotStart) {
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
            double neededRoom = initSolution.getNeededRoomBySubject(s);
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
                        if (!studentEachSlot.addAll(initSolution.getStudentsCurrentSubject(s))) {
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
        // H5: Số giám thị xếp cho mỗi môn phải bằng số phòng của môn đó tại mỗi slot
        for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
            for (int t = subjectSlotStart[s]; t <= subjectSlotStart[s] + ImplementData.SUBJECT_DURATION_VECTOR[s] - 1; t++) {
                int numberOfProctor = 0;
                for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                    numberOfProctor += solution[s][t][g];
                }
                int neededRoom = initSolution.getNeededRoomBySubject(s);
                if (numberOfProctor != neededRoom) {
                    numberOfViolate++;
                }
            }
        }

        //H6: Trong cùng một ca giám thị chỉ được trông một môn
        for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
            for (int t = 1; t <= ImplementData.TOTAL_EXAM_SLOTS; t++) {
                // Số lượng môn giám thị đó phải trông
                int subjectEachSlot = 0;
                for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
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
        for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
            if (initSolution.isDurationConflict(subjectSlotStart[s], s)) {
                numberOfViolate++;
            }
        }
        // TO-DO H9, H10: Để làm sau
        return numberOfViolate;
    }

    // Boolean check hardCosntraint, hardConstraint nào check nhanh thì cho lên check trước
    private boolean checkHardConstraintBoolean(int[][][] solution, int[] subjectSlotStart) {

        // H8: Các môn có duration > 1 cần sắp sao cho môn đó phải tổ chức trong cùng một buổi
        for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
            if (initSolution.isDurationConflict(subjectSlotStart[s], s)) {
                return false;
            }
        }

        // H9, H10: Sắp thì phải thỏa mãn các môn định slot trước
        Map<Integer, Integer> specialSubjectSlot = initSolution.getSpecialSubjectSlot();
        for (Map.Entry<Integer, Integer> entry : specialSubjectSlot.entrySet()) {
            int subject = entry.getKey();
            int slotStartSpecial = entry.getValue();
            boolean isValid = false;
            for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                if(solution[subject][slotStartSpecial][g] == 1){
                    isValid = true;
                    break;
                }
            }
            if(!isValid){
                return false;
            }
        }

        // H4: Với mỗi môn thi, giám thị được phân công cần trông hết cả các slot liên tiếp mà môn thi đó diễn ra
        // Phải lấy ra được Ts
        for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
            for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                for (int t = subjectSlotStart[s]; t <= subjectSlotStart[s] + ImplementData.SUBJECT_DURATION_VECTOR[s] - 2; t++) {
                    if (solution[s][t][g] - solution[s][t + 1][g] != 0) {
                        return false;
                    }
                }
            }
        }

        // H5: Số giám thị xếp cho mỗi môn phải bằng số phòng của môn đó tại mỗi slot
        for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
            for (int t = subjectSlotStart[s]; t <= subjectSlotStart[s] + ImplementData.SUBJECT_DURATION_VECTOR[s] - 1; t++) {
                int numberOfProctor = 0;
                for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                    numberOfProctor += solution[s][t][g];
                }
                int neededRoom = initSolution.getNeededRoomBySubject(s);
                if (numberOfProctor != neededRoom) {
                    return false;
                }
            }
        }


        // H1: Tất cả các môn đều được sắp lịch thi
        for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
            // Số giám thị được phân thực tế
            double numberProctorEachSubject = 0;
            for (int t = 1; t <= ImplementData.TOTAL_EXAM_SLOTS; t++) {
                for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                    numberProctorEachSubject += solution[s][t][g];
                }
            }
            // Số phòng cần tổ chức thi của môn này
            double neededRoom = initSolution.getNeededRoomBySubject(s);
            // Số giám thị cần phải phân theo kế hoạch
            double neededProctor = ImplementData.SUBJECT_DURATION_VECTOR[s] * neededRoom;
            if (numberProctorEachSubject != neededProctor) {
                return false;
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
                return false;
            }
        }


        //H6: Trong cùng một ca giám thị chỉ được trông một môn
        for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
            for (int t = 1; t <= ImplementData.TOTAL_EXAM_SLOTS; t++) {
                // Số lượng môn giám thị đó phải trông
                int subjectEachSlot = 0;
                for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
                    subjectEachSlot += solution[s][t][g];
                }
                if (subjectEachSlot > 1) {
                    return false;
                }
            }
        }

        // H7: Giám thị chỉ được phân môn họ có thể trông được,
        for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
            for (int t = 1; t <= ImplementData.TOTAL_EXAM_SLOTS; t++) {
                for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                    if (solution[s][t][g] > ImplementData.PROCTOR_SUBJECT_MATRIX[g][s]) {
                        return false;
                    }
                }
            }
        }


        //H2: Trong cùng một ca sinh viên không được thi 2 môn khác nhau
        for (int t = 1; t <= ImplementData.TOTAL_EXAM_SLOTS; t++) {
            Set<Integer> studentEachSlot = new HashSet<>();
            for (int s = 1; s <= ImplementData.NUMBER_OF_SUBJECT; s++) {
                for (int g = 1; g <= ImplementData.NUMBER_OF_PROCTOR; g++) {
                    if (solution[s][t][g] == 1) {
                        if (!studentEachSlot.addAll(initSolution.getStudentsCurrentSubject(s))) {
                            return false;
                        }
                        break;
                    }
                }
            }
        }


        return true;
    }
}
