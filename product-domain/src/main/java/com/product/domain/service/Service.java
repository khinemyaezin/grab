package com.product.domain.service;

public class Service {
//    public List<List<VariantOption>> generateCombinations(List<VariantType> variantTypes) {
//        List<List<VariantOption>> optionLists = new ArrayList<>();
//        for (VariantType VariantType : variantTypes) {
//            optionLists.add(VariantType.getOptions());
//        }
//
//        List<List<VariantOption>> combinations = new ArrayList<>();
//        int totalCombinations = optionLists.stream()
//                .mapToInt(List::size)
//                .reduce(1, Math::multiplyExact);
//
//        for (int i = 0; i < totalCombinations; i++) {
//            List<VariantOption> combination = new ArrayList<>();
//            int divisor = 1;
//            for (List<VariantOption> options : optionLists) {
//                int index = (i / divisor) % options.size();
//                combination.add(options.get(index));
//                divisor *= options.size();
//            }
//            combinations.add(combination);
//        }
//
//        return combinations;
//    }
}
