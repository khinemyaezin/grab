package com.grab.store.catalog.internal.util;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class MatrixReconciliationService{

    private List<String> reconcile(List<String> currentActive,
                                  Map<String, List<String>> oldMatrix,
                                  Map<String, List<String>> newMatrix) {

        // 1. Generate the full "Theoretical" sets for both states
        Set<String> oldUniverse = generateAllPossible(oldMatrix);
        Set<String> newUniverse = generateAllPossible(newMatrix);

        // 2. Identify combinations that are truly new (New - Old)
        // This avoids re-adding things that were deleted in the past
        Set<String> newlyAdded = new LinkedHashSet<>(newUniverse);
        newlyAdded.removeAll(oldUniverse);

        // 3. Combine current active items with the brand new ones
        List<String> result = new ArrayList<>(currentActive);
        result.addAll(newlyAdded);
        if(oldMatrix.size() > newMatrix.size()){
            result.removeAll(oldUniverse);
        }


        return result;
    }

    private Set<String> generateAllPossible(Map<String, List<String>> matrix) {
        List<List<String>> values = new ArrayList<>(matrix.values());
        Set<String> combinations = new LinkedHashSet<>();
        generateRecursive(values, 0, "", combinations);
        return combinations;
    }

    private void generateRecursive(List<List<String>> allValues, int depth, String current, Set<String> results) {
        if (depth == allValues.size()) {
            results.add(current.substring(0, current.length() - 1));
            return;
        }
        for (String val : allValues.get(depth)) {
            generateRecursive(allValues, depth + 1, current + val + "-", results);
        }
    }
    @Test
    public void problemOne() {
        MatrixReconciliationService service = new MatrixReconciliationService();

        /**
         *   color [ y ]
         *   size [ m, s ]
         *   num [ 1, 2 ]
         */
        // State 1: Active items (v2-v1 is already missing/deleted)
        List<String> currentActive = new ArrayList<>(Arrays.asList(
                "y-m-1",
                "y-m-2",
                "y-s-2"
        ));

        // State 2: Metadata maps
        Map<String, List<String>> oldMatrix = new LinkedHashMap<>();
        oldMatrix.put("c", List.of("y"));
        oldMatrix.put("s", Arrays.asList("m", "s"));
        oldMatrix.put("n", Arrays.asList("1", "2"));

        /**
         *   color [ y, b ]
         *   size [ m, s ]
         *   num [ 1, 2 ]
         */
        Map<String, List<String>> newMatrix = new LinkedHashMap<>();
        newMatrix.put("c", List.of("y", "b"));
        newMatrix.put("s", Arrays.asList("m", "s"));
        newMatrix.put("n", Arrays.asList("1", "2"));

        List<String> output = service.reconcile(currentActive, oldMatrix, newMatrix);

        output.forEach(System.out::println);
        Assertions.assertArrayEquals(new String[]{
                "y-m-1",
                "y-m-2",
                "y-s-2",
                "b-m-1",
                "b-m-2",
                "b-s-1",
                "b-s-2"
        }, output.toArray());
    }
    @Test
    public void problemTwo() {
        MatrixReconciliationService service = new MatrixReconciliationService();

        /**
         *   color [ y ]
         *   size [ m, s ]
         *   num [ 1, 2 ]
         */
        // State 1: Active items (v2-v1 is already missing/deleted)
        List<String> currentActive = new ArrayList<>(Arrays.asList(
                "y-m-1",
                "y-m-2",
                "y-s-2"
        ));

        // State 2: Metadata maps
        Map<String, List<String>> oldMatrix = new LinkedHashMap<>();
        oldMatrix.put("c", List.of("y"));
        oldMatrix.put("s", Arrays.asList("m", "s"));
        oldMatrix.put("n", Arrays.asList("1", "2"));

        /**
         *   color [ y, b ]
         *   size [ m, s ]
         *   num [ 1, 2 ]
         */
        Map<String, List<String>> newMatrix = new LinkedHashMap<>();
        //newMatrix.put("c", List.of("y"));
        newMatrix.put("s", Arrays.asList("m", "s"));
        newMatrix.put("n", Arrays.asList("1", "2"));

        List<String> output = service.reconcile(currentActive, oldMatrix, newMatrix);

        output.forEach(System.out::println);
        Assertions.assertArrayEquals(new String[]{
                "m-1",
                "m-2",
                "s-1",
                "s-2"
        }, output.toArray());
    }

    @Test
    public void problemThree() {
        MatrixReconciliationService service = new MatrixReconciliationService();

        /**
         *   color [ y ]
         *   size [ m, s ]
         *   num [ 1, 2 ]
         */
        // State 1: Active items (v2-v1 is already missing/deleted)
        List<String> currentActive = new ArrayList<>(Arrays.asList(
                "y-m-1",
                "y-m-2",
                "y-s-2"
        ));

        // State 2: Metadata maps
        Map<String, List<String>> oldMatrix = new LinkedHashMap<>();
        oldMatrix.put("c", List.of("y"));
        oldMatrix.put("s", Arrays.asList("m", "s"));
        oldMatrix.put("n", Arrays.asList("1", "2"));

        /**
         *   color [ y, b ]
         *   size [ m, s ]
         *   num [ 1, 2 ]
         */
        Map<String, List<String>> newMatrix = new LinkedHashMap<>();
        newMatrix.put("c", List.of("y"));
        newMatrix.put("s", Arrays.asList("m", "s"));
        newMatrix.put("n", Arrays.asList("1", "2"));

        List<String> output = service.reconcile(currentActive, oldMatrix, newMatrix);

        output.forEach(System.out::println);
        Assertions.assertArrayEquals(new String[]{
                "y-m-1",
                "y-m-2",
                "y-s-2"
        }, output.toArray());
    }

    @Test
    public void problemFour() {
        MatrixReconciliationService service = new MatrixReconciliationService();

        /**
         *   color [ y ]
         *   size [ m, s ]
         *   num [ 1, 2 ]
         */
        // State 1: Active items (v2-v1 is already missing/deleted)
        List<String> currentActive = new ArrayList<>(Arrays.asList(
                "y-m-1",
                "y-m-2",
                "y-s-2"
        ));

        // State 2: Metadata maps
        Map<String, List<String>> oldMatrix = new LinkedHashMap<>();
        oldMatrix.put("c", List.of("y"));
        oldMatrix.put("s", Arrays.asList("m", "s"));
        oldMatrix.put("n", Arrays.asList("1", "2"));

        /**
         *   color [ y, b ]
         *   size [ m, s ]
         *   num [ 1, 2 ]
         */
        Map<String, List<String>> newMatrix = new LinkedHashMap<>();
        newMatrix.put("c", List.of("y"));
        newMatrix.put("s", Arrays.asList("m", "s"));
        //newMatrix.put("n", Arrays.asList("1", "2"));

        List<String> output = service.reconcile(currentActive, oldMatrix, newMatrix);

        output.forEach(System.out::println);
        Assertions.assertArrayEquals(new String[]{
                "y-m",
                "y-s"
        }, output.toArray());
    }

    @Test
    public void problemFive() {
        MatrixReconciliationService service = new MatrixReconciliationService();

        /**
         *   color [ y ]
         *   size [ m, s ]
         *   num [ 1, 2 ]
         */
        // State 1: Active items (v2-v1 is already missing/deleted)
        List<String> currentActive = new ArrayList<>(Arrays.asList(
                "y-m-2",
                "y-s-2"
        ));

        // State 2: Metadata maps
        Map<String, List<String>> oldMatrix = new LinkedHashMap<>();
        oldMatrix.put("c", List.of("y"));
        oldMatrix.put("s", Arrays.asList("m", "s"));
        oldMatrix.put("n", Arrays.asList("1", "2"));

        /**
         *   color [ y, b ]
         *   size [ m, s ]
         *   num [ 1, 2 ]
         */
        Map<String, List<String>> newMatrix = new LinkedHashMap<>();
        newMatrix.put("c", List.of("y", "b"));
        newMatrix.put("s", Arrays.asList("m", "s"));
        newMatrix.put("n", Arrays.asList("1", "2"));

        List<String> output = service.reconcile(currentActive, oldMatrix, newMatrix);

        output.forEach(System.out::println);
        Assertions.assertArrayEquals(new String[]{
                "y-m-2",
                "y-s-2",
                "b-m-1",
                "b-m-2",
                "b-s-1",
                "b-s-2"
        }, output.toArray());
    }

    @Test
    public void problemSix() {
        MatrixReconciliationService service = new MatrixReconciliationService();

        /**
         *   color [ y ]
         *   size [ m, s ]
         *   num [ 1, 2 ]
         */
        // State 1: Active items (v2-v1 is already missing/deleted)
        List<String> currentActive = new ArrayList<>(Arrays.asList(
                //"y-m-1",
                "y-m-2",
                //"y-m-2",
                "y-s-2"
        ));

        // State 2: Metadata maps
        Map<String, List<String>> oldMatrix = new LinkedHashMap<>();
        oldMatrix.put("c", List.of("y"));
        oldMatrix.put("s", Arrays.asList("m", "s"));
        oldMatrix.put("n", Arrays.asList("1", "2"));

        /**
         *   color [ y, b ]
         *   size [ m, s ]
         *   num [ 1, 2 ]
         */
        Map<String, List<String>> newMatrix = new LinkedHashMap<>();
        newMatrix.put("c", List.of("y"));
        newMatrix.put("s", Arrays.asList("m", "s"));
        newMatrix.put("n", List.of("1"));

        List<String> output = service.reconcile(currentActive, oldMatrix, newMatrix);

        output.forEach(System.out::println);
        Assertions.assertArrayEquals(new String[]{}, output.toArray());
    }
}