package com.example.AlgorithmModuleImpl.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
@Service
public class AlgorithmUltils {
    public int getRandomInListInteger(List<Integer> listRandomSlot) {
        // Sử dụng Random để tạo chỉ mục ngẫu nhiên
        Random random = new Random();
        int randomIndex = random.nextInt(listRandomSlot.size());

        // Trả về phần tử tại chỉ mục ngẫu nhiên
        return listRandomSlot.get(randomIndex);
    }

    public String convertArrayToString(int[] subjectSlotStartCurrent) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int num : subjectSlotStartCurrent) {
            stringBuilder.append(num).append(",");
        }
        return stringBuilder.toString();
    }
}
