package com.S209.yobi.domain.measures.service;

import com.S209.yobi.domain.measures.helper.BodyCompResultVo;
import com.S209.yobi.domain.measures.helper.Utils;
import com.S209.yobi.domain.measures.helper.tbl_bodycomp;
import com.S209.yobi.domain.measures.helper.Continents;
public class BodyRangeCalculator {

    public static BodyCompResultVo setGenValues(String continent, tbl_bodycomp bodycomp, BodyCompResultVo vo) {
        boolean isMale = Utils.isMale(bodycomp.getUGender());
        int genBfp = genFatPercentage(continent, isMale, bodycomp.getUAge(), bodycomp.getBfp());
        int genBfm = genFatMass(continent, isMale, bodycomp.getUAge(), bodycomp.getUWeight(), bodycomp.getBfm());
        int genSmm = genMuscleMass(isMale, bodycomp.getUAge(), bodycomp.getUWeight(), bodycomp.getSmm());
        int genBmr = genBmr(bodycomp.getUWeight(), bodycomp.getBfp(), bodycomp.getBmr());
//        int genBmi = genBmi(continent, bodycomp.getUHeight().intValue(), bodycomp.getBmi());
//        int genWeight = genWeight(isMale, bodycomp.getUAge(), bodycomp.getUHeight().intValue(), bodycomp.getUWeight());
//        int genBwp = genBodyWater(bodycomp.getBwp(), isMale ? "male" : "female", bodycomp.getUAge(), bodycomp.getUHeight().intValue());
        int genProtein = genProtein(isMale, bodycomp.getUAge(), bodycomp.getUWeight(), bodycomp.getProtein());
        int genMinerals = genMinerals(isMale, bodycomp.getUAge(), bodycomp.getUWeight(), bodycomp.getMinerals());
        int genEcf = genEcf(bodycomp.getEcf());

        vo.setGenBfp(genBfp);
        vo.setGenBfm(genBfm);
        vo.setGenSmm(genSmm);
        vo.setGenBmr(genBmr);
//        vo.setGenBmi(genBmi);
//        vo.setGenWeight(genWeight);
//        vo.setGenBwp(genBwp);
        vo.setGenProtein(genProtein);
        vo.setGenMinerals(genMinerals);
        vo.setGenEcf(genEcf);

        return vo;
    }

    public static double getFatStandard(String continent, boolean isMale, int age) {
        int[] boundary = getBfpRange(continent, isMale, age);
        return (boundary[1] + boundary[2]) / 2.0f;
    }

    public static double getFatMassStandard(String continent, boolean isMale, int age, double kg) {
        double[] boundary = getBfmRange2(continent, isMale, age, kg);
        return (boundary[1] + boundary[2]) / 2.0f;
    }

    public static double getMuscleMassStandard(boolean isMale, int age) {
        int[] boundary = getSmmRange(isMale, age);
        return (boundary[1] + boundary[2]) / 2.0f;
    }

    public static double getBmrStandard(float weight, float bfp) {
        int[] boundary = getBmrRange(weight, bfp);
        return (boundary[1] + boundary[2] / 2.0f);
    }

    public static double getBmiStandard(String continent, float height) {
        int[] boundary = getBmiRange(continent, height);
        return (boundary[1] + boundary[2]) / 2.0f;
    }

    public static double getWeightStandard(boolean isMale, int age, float height) {
        int[] boundary = getWeightRange(isMale, age, height);
        return (boundary[1] + boundary[2]) / 2.0f;
    }

//    public static double getBodyWaterStandard(String gender, int age, int cm) {
//        return BodyStandardCalculator.bodyWaterPerBoundary(0.0, gender, age, (double) cm).get("normal");
//    }

    public static int genFatPercentage(String continent, boolean isMale, int age, double fat_per) {
        int[] boundary = getBfpRange(continent, isMale, age);
        return calGenValue(boundary, fat_per);
    }

    public static int genFatMass(String continent, boolean isMale, int age, double kg, double bfm) {
        double[] boundary = getBfmRange2(continent, isMale, age, kg);
        return calGenValue(boundary, bfm);
    }

    public static int genFatMass(String continent, boolean isMale, int age, double kg, float bfm) {
        float[] boundary = getBfmRange(continent, isMale, age, kg);
        return calGenValue(boundary, bfm);
    }

    public static int genMuscleMass(boolean isMale, int age, double smm) {
        int[] boundary = getSmmRange(isMale, age);
        return calGenValue(boundary, smm);
    }

    public static int genMuscleMass(boolean isMale, double smm) {
        int[] boundary = getSmmRange(isMale);
        return calGenValue(boundary, smm);
    }

    public static int genMuscleMass(boolean isMale, int age, float weight, double smm) {
        double[] boundary = getSmmRange(isMale, age, weight);
        return calGenValue(boundary, smm);
    }

    public static int genBmr(float weight, double bfp, double bmr) {
        int[] boundary = getBmrRange(weight, bfp);
        return calGenValue(boundary, bmr);
    }

    public static int genBmr(float weight, float bfp, double bmr) {
        int[] boundary = getBmrRange(weight, bfp);
        return calGenValue(boundary, bmr);
    }

    public static int genBmi(String continent, float height, double bmi) {
        int[] boundary = getBmiRange(continent, height);
        return calGenValue(boundary, bmi);
    }

    public static int genWeight(boolean isMale, int age, float height, float weight) {
        int[] boundary = getWeightRange(isMale, age, height);
        return calGenValue(boundary, weight);
    }

//    public static int genBodyWater(double bodyWaterPer, String gender, int age, int cm) {
//        int[] boundary = getBodyWaterRange(bodyWaterPer, gender, age, cm);
//        return calGenValue(boundary, bodyWaterPer);
//    }

//    public static int genBodyWater(boolean isMale, int age, double weight, double bodyWater) {
//        int[] boundary = getBodyWaterRange(isMale, age, weight);
//        return calGenValue(boundary, bodyWater);
//    }

    public static int genBodyWater(boolean isMale, int age, double bodyWater) {
        int[] boundary = getBodyWaterRange(isMale, age);
        return calGenValue(boundary, bodyWater);
    }
    public static int genProtein(boolean isMale, int age, float weight, double bodyProtein) {
        double[] boundary = getProRange(isMale, age, weight);
        return calGenValue(boundary, bodyProtein);
    }

    public static int genMinerals(boolean isMale, int age, float weight, double bodyMinerals) {
        double[] boundary = getMineralsRange(isMale, age, weight);
        return calGenValue(boundary, bodyMinerals);
    }

    public static int genEcf(double bodyEcf) {
        int[] boundary = getEcfRange();
        return calGenValue(boundary, bodyEcf);
    }

    // 5개 바운더리
    // 60 ~ 180 범위로 변환
    private static int calGenValue(float[] boundary, double value) {
        int valueIndex = boundary.length;
        for (int i = 0; i < boundary.length; ++i) {
            if (boundary[i] > value) {
                valueIndex = i - 1;
                break;
            }
        }

        if (valueIndex < 0)
            return 60;
        if (valueIndex >= boundary.length)
            return 180;

        final float valueStart = boundary[valueIndex];
        final float valueEnd = boundary[valueIndex + 1];
        final float valueRange = valueEnd - valueStart;
        final double percent = (valueIndex + ((value - valueStart) / valueRange)) / (boundary.length - 1);

        return 60 + (int)(percent * 120);
    }

    private static int calGenValue(double[] boundary, double value) {
        int valueIndex = boundary.length;
        for (int i = 0; i < boundary.length; ++i) {
            if (boundary[i] > value) {
                valueIndex = i - 1;
                break;
            }
        }

        if (valueIndex < 0)
            return 60;
        if (valueIndex >= boundary.length)
            return 180;

        final double valueStart = boundary[valueIndex];
        final double valueEnd = boundary[valueIndex + 1];
        final double valueRange = valueEnd - valueStart;
        final double percent = (valueIndex + ((value - valueStart) / valueRange)) / (boundary.length - 1);

        return 60 + (int)(percent * 120);
    }

    private static int calGenValue(int[] boundary, double value) {
        int valueIndex = boundary.length;
        for (int i = 0; i < boundary.length; ++i) {
            if (boundary[i] > value) {
                valueIndex = i - 1;
                break;
            }
        }

        if (valueIndex < 0)
            return 60;
        if (valueIndex >= boundary.length)
            return 180;

        final int valueStart = boundary[valueIndex];
        final int valueEnd = boundary[valueIndex + 1];
        final int valueRange = valueEnd - valueStart;
        final double percent = (valueIndex + ((value - valueStart) / valueRange)) / (boundary.length - 1);

        return 60 + (int)(percent * 120);
    }

    public static String countryToContinent(String countryCode, String countryName, String lang) {
        String continent;
        continent = Continents.countryToContinent(countryCode);
        if (continent == null && countryName != null)
            continent = Continents.countryToContinent(countryName);
        if (continent == null && lang != null)
            try {
                String country = lang.substring(3, 5);
                continent = Continents.countryToContinent(country);
            } catch (Exception e) {
            }
        if (continent == null)
            continent = Continents.CONTINENT_NORTH_AMERICA;
        return continent;
    }

    public static int[] getBfpRange(String continent, boolean isMale, int age) {
        return getBoundary(BFP_MIN_AGE_TABLE, getBfpValueTable(continent, isMale), age);
    }

    public static double[] getBfmRange2(String continent, boolean isMale, int age, double kg) {
        int[] boundBfp = getBfpRange(continent, isMale, age);
        double[] boundBfm = new double[boundBfp.length];
        for (int i = 0; i < boundBfp.length; ++i)
            boundBfm[i] = Math.round(boundBfp[i] * kg / 10) / 10f;
        return boundBfm;
    }

    public static float[] getBfmRange(String continent, boolean isMale, int age, double kg) {
        int[] boundBfp = getBfpRange(continent, isMale, age);
        float[] boundBfm = new float[boundBfp.length];
        for (int i = 0; i < boundBfp.length; ++i)
            boundBfm[i] = Math.round(boundBfp[i] * kg / 10) / 10f;
        return boundBfm;
    }

    public static int[] getSmmRange(boolean isMale, int age) {
        return getBoundary(MUSCLE_MIN_AGE_TABLE, isMale ? MUSCLE_MIN_VALUE_MALE_TABLE : MUSCLE_MIN_VALUE_FEMALE_TABLE, age);
    }

    public static int[] getSmmRange(boolean isMale) {
        return isMale? SMM_VALUE_MALE_TABLE : SMM_VALUE_FEMALE_TABLE;
    }

    public static double[] getSmmRange(boolean isMale, int age, float weight) {
        // 골격근율 * 100 / 몸무게 = 골격근량
        double[] smm_ratio_boundary = getBoundary(MUSCLE_MIN_AGE_TABLE, isMale ? MUSCLE_RATIO_MIN_VALUE_MALE_TABLE : MUSCLE_RATIO_MIN_VALUE_FEMALE_TABLE, age);
        double[] smm_boundary = new double[smm_ratio_boundary.length];

        for(int i = 0; i < smm_ratio_boundary.length; i++)
            smm_boundary[i] = smm_ratio_boundary[i] / 100 * weight;

        return smm_boundary;
    }

    public static int[] getBmrRange(float weight, double bfp) {
        final double bfm = weight * bfp / 100;
        final double lbm = weight - bfm;
        final double meanBmr = 21.6 * lbm + 370;

        return new int[]{(int) (0.6 * meanBmr), (int) (0.85 * meanBmr), (int) (1.2 * meanBmr), (int) (1.4 * meanBmr), (int) (1.6 * meanBmr)};
    }

    public static int[] getBmrRange(float weight, float bfp) {
        final float bfm = weight * bfp / 100;
        final float lbm = weight - bfm;
        final float meanBmr = 21.6f * lbm + 370;

        return new int[]{(int) (0.6f * meanBmr), (int) (0.85 * meanBmr), (int) (1.2 * meanBmr), (int) (1.4 * meanBmr), (int) (1.6 * meanBmr)};
    }

    public static int[] getBmiRange(String continent, float height_cm) {
        return getBoundary(getBmiHeightTable(continent), getBmiValueTable(continent), height_cm);
    }

    public static int[] getWeightRange(boolean isMale, int age, float height_cm) {
        return getBoundary(getWeightHeightTable(age, isMale), getWeightValueTable(age, isMale), height_cm);
    }

    public static double[] getProRange(boolean isMale, int age, float weight) {
        int[] protein_ratio_boundary = getBoundary(PRO_AGE_TABLE, isMale ? PRO_RATIO_VALUE_MALE_TABLE : PRO_RATIO_VALUE_FEMALE_TABLE, age);
        double[] protein_boundary = new double[protein_ratio_boundary.length];

        for(int i = 0; i < protein_ratio_boundary.length; i++)
            protein_boundary[i] = (double)protein_ratio_boundary[i] / 100 * weight;
        return protein_boundary;
    }

    public static double[] getMineralsRange(boolean isMale, int age, float weight) {
        int[] mineral_ratio_boundary = getBoundary(MINERAL_AGE_TABLE, isMale ? MINERAL_RATIO_VALUE_MALE_TABLE : MINERAL_RATIO_VALUE_FEMALE_TABLE, age);
        double[] mineral_boundary = new double[mineral_ratio_boundary.length];

        for(int i = 0; i < mineral_ratio_boundary.length; i++)
            mineral_boundary[i] = (double)mineral_ratio_boundary[i] / 100 * weight;
        return mineral_boundary;
    }

    public static int[] getEcfRange() {
        return ECF_VALUE_TABLE;
    }

    //From Smart diet
//    public static int[] getBodyWaterRange(double bodyWaterPer, String gender, int age, int cm) {
//        double normalVal = BodyStandardCalculator.bodyWaterPerBoundary(bodyWaterPer, gender, age, (double) cm).get("normal");
//
//        return new int[]{(int) (0.6f * normalVal), (int) (0.85 * normalVal), (int) (1.15 * normalVal), (int) (1.4 * normalVal), (int) (1.6 * normalVal)};
//    }

    public static int[] getBodyWaterRange(boolean isMale, int age, float weight) {
        int[] bw_ratio_boundary = getBoundary(BW_AGE_TABLE, isMale ? BW_RATIO_VALUE_MALE_TABLE : BW_RATIO_VALUE_FEMALE_TABLE, age);
        int[] bw_boundary = new int[bw_ratio_boundary.length];

        for(int i = 0; i < bw_ratio_boundary.length; i++)
            bw_boundary[i] = (int) ((double)bw_ratio_boundary[i] / 100 * weight);
        return bw_boundary;
    }

    public static int[] getBodyWaterRange(boolean isMale, int age) {
        return getBoundary(BW_AGE_TABLE, isMale ? BW_RATIO_VALUE_MALE_TABLE : BW_RATIO_VALUE_FEMALE_TABLE, age);
    }

    //////////////////////////////////
    // 계산을 위해 필요한 메소드
    //////////////////////////////////=
    public static int[] getBoundary(int[] minIndexTable, int[][] minValueTable, float index) {
        int tableIndex = 0;

        for (int i = minIndexTable.length - 1; i > 0; --i) {
            if (minIndexTable[i] <= index) {
                tableIndex = i;
                break;
            }
        }
        return minValueTable[tableIndex];
    }

    public static double[] getBoundary(int[] minIndexTable, double[][] minValueTable, int index) {
        int tableIndex = 0;

        for (int i = minIndexTable.length - 1; i > 0; --i) {
            if (minIndexTable[i] <= index) {
                tableIndex = i;
                break;
            }
        }
        return minValueTable[tableIndex];
    }

    public static int getIndex(int[] minIndexTable, double value) {
        int tableIndex = 0;

        for (int i = minIndexTable.length - 1; i > 0; --i) {
            if (minIndexTable[i] <= value) {
                tableIndex = i;
                break;
            }
        }
        return tableIndex;
    }

    public static float getMeanValue(int[] table) {
        final int low = table[1];
        final int high = table[2];
        return (low + high) / 2f;
    }

    // 근골격량
    public final static int[] MUSCLE_MIN_AGE_TABLE = {14, 17, 21, 26, 31, 36, 41, 46, 51, 56, 61, 66, 71, 75};
    public final static int[][] MUSCLE_MIN_VALUE_MALE_TABLE = {
            {17, 25, 36, 43, 49},
            {17, 25, 36, 43, 50},
            {19, 28, 39, 46, 52},
            {20, 28, 40, 47, 54},
            {21, 29, 39, 46, 52},
            {20, 28, 38, 44, 51},
            {20, 27, 37, 43, 49},
            {20, 27, 37, 43, 49},
            {21, 29, 38, 44, 51},
            {21, 28, 37, 43, 49},
            {21, 28, 37, 43, 49},
            {21, 28, 36, 42, 47},
            {20, 26, 33, 38, 43},
            {19, 25, 32, 37, 42}
    };
    public final static int[][] MUSCLE_MIN_VALUE_FEMALE_TABLE = {
            {13, 18, 25, 29, 33},
            {13, 18, 25, 29, 33},
            {15, 20, 27, 31, 36},
            {15, 21, 28, 32, 37},
            {16, 21, 28, 33, 37},
            {16, 22, 29, 34, 38},
            {15, 21, 29, 33, 38},
            {15, 20, 28, 33, 37},
            {15, 21, 27, 31, 35},
            {15, 20, 27, 32, 36},
            {14, 18, 25, 29, 33},
            {14, 18, 25, 29, 33},
            {13, 17, 23, 27, 30},
            {12, 16, 21, 25, 28}
    };
    // 2024.6.26 Excel 값 적용. By 이대호 CEO
    public final static double[][] MUSCLE_RATIO_MIN_VALUE_MALE_TABLE = {
            {21.2, 42.5, 53.1, 63.7, 82.8},
            {21.0, 42.0, 52.6, 63.1, 82.0},
            {20.8, 41.6, 52.0, 62.4, 81.2},
            {20.6, 41.2, 51.5, 61.8, 80.4},
            {20.4, 40.8, 51.0, 61.2, 79.6},
            {20.2, 40.4, 50.5, 60.6, 78.8},
            {20.0, 40.0, 50.0, 60.0, 78.0},
            {19.8, 39.6, 49.5, 59.4, 77.2},
            {19.6, 39.2, 49.0, 58.8, 76.4},
            {19.4, 38.8, 48.5, 58.2, 75.7},
            {19.2, 38.4, 48.0, 57.6, 74.9},
            {19.0, 38.0, 47.5, 57.1, 74.2},
            {18.8, 37.7, 47.1, 56.5, 73.4},
            {18.6, 37.3, 46.6, 55.9, 72.7}
    };
    public final static double[][] MUSCLE_RATIO_MIN_VALUE_FEMALE_TABLE = {
            {15.9, 31.8, 42.5, 53.1, 69.0},
            {15.8, 31.5, 42.0, 52.6, 68.3},
            {15.6, 31.2, 41.6, 52.0, 67.6},
            {15.5, 30.9, 41.2, 51.5, 67.0},
            {15.3, 30.6, 40.8, 51.0, 66.3},
            {15.2, 30.3, 40.4, 50.5, 65.7},
            {15.0, 30.0, 40.0, 50.0, 65.0},
            {14.9, 29.7, 39.6, 49.5, 64.4},
            {14.7, 29.4, 39.2, 49.0, 63.7},
            {14.6, 29.1, 38.8, 48.5, 63.1},
            {14.4, 28.8, 38.4, 48.0, 62.4},
            {14.3, 28.5, 38.0, 47.5, 61.8},
            {14.1, 28.2, 37.7, 47.1, 61.2},
            {14.0, 28.0, 37.3, 46.6, 60.6}
    };

    // 체중
    public static int[] getWeightHeightTable(int age, boolean isMale) {
        if (age >= 18) {
            return isMale ? WEIGHT_MIN_HEIGHT_MALE_OVER18_TABLE : WEIGHT_MIN_HEIGHT_FEMALE_OVER18_TABLE;
        } else {
            return isMale ? WEIGHT_MIN_HEIGHT_MALE_UNDER18_TABLE : WEIGHT_MIN_HEIGHT_FEMALE_UNDER18_TABLE;
        }
    }

    public static int[][] getWeightValueTable(int age, boolean isMale) {
        if (age >= 18) {
            return isMale ? WEIGHT_MIN_VALUE_MALE_OVER18_TABLE : WEIGHT_MIN_VALUE_FEMALE_OVER18_TABLE;
        } else {
            return isMale ? WEIGHT_MIN_VALUE_MALE_UNDER18_TABLE : WEIGHT_MIN_VALUE_FEMALE_UNDER18_TABLE;
        }
    }

    public final static int[] WEIGHT_MIN_HEIGHT_MALE_UNDER18_TABLE = {100, 110, 114, 122, 125, 130, 135, 140, 148, 152, 157, 160, 162, 165};
    public final static int[] WEIGHT_MIN_HEIGHT_MALE_OVER18_TABLE = {150, 162, 165, 168, 170, 173, 175, 178, 180, 183, 185, 188, 190, 193};
    public final static int[] WEIGHT_MIN_HEIGHT_FEMALE_UNDER18_TABLE = {100, 110, 114, 122, 125, 130, 135, 140, 145, 150, 152, 153};
    public final static int[] WEIGHT_MIN_HEIGHT_FEMALE_OVER18_TABLE = {140, 152, 155, 157, 160, 162, 165, 168, 170, 173, 175, 178, 180, 182};
    public final static int[][] WEIGHT_MIN_VALUE_MALE_UNDER18_TABLE = {
            {8, 12, 17, 20, 23},
            {9, 13, 19, 22, 26},
            {10, 15, 21, 25, 28},
            {11, 16, 23, 27, 31},
            {12, 18, 25, 29, 34},
            {14, 19, 28, 32, 37},
            {15, 21, 30, 35, 40},
            {17, 24, 34, 39, 45},
            {19, 27, 38, 44, 51},
            {21, 30, 42, 49, 57},
            {23, 33, 47, 55, 63},
            {28, 38, 54, 62, 71},
            {29, 41, 57, 66, 75},
            {30, 42, 58, 68, 77}
    };
    public final static int[][] WEIGHT_MIN_VALUE_MALE_OVER18_TABLE = {
            {32, 46, 69, 81, 93},
            {33, 48, 73, 85, 97},
            {34, 50, 75, 88, 100},
            {35, 51, 77, 90, 103},
            {36, 53, 79, 92, 106},
            {37, 54, 81, 95, 109},
            {38, 56, 84, 98, 112},
            {39, 57, 86, 100, 115},
            {40, 58, 88, 103, 117},
            {41, 60, 90, 106, 121},
            {42, 62, 93, 109, 124},
            {48, 68, 100, 116, 132},
            {49, 70, 103, 120, 136},
            {50, 71, 105, 121, 138}
    };
    public final static int[][] WEIGHT_MIN_VALUE_FEMALE_UNDER18_TABLE = {
            {8, 12, 17, 20, 23},
            {9, 13, 19, 22, 25},
            {10, 14, 21, 24, 28},
            {11, 16, 23, 27, 31},
            {12, 18, 25, 29, 34},
            {14, 20, 28, 33, 37},
            {15, 22, 31, 36, 42},
            {17, 25, 35, 41, 47},
            {19, 28, 39, 46, 53},
            {23, 33, 46, 53, 60},
            {26, 36, 51, 59, 67},
            {27, 38, 53, 61, 69}
    };
    public final static int[][] WEIGHT_MIN_VALUE_FEMALE_OVER18_TABLE = {
            {25, 38, 56, 66, 76},
            {26, 39, 57, 68, 78},
            {29, 42, 61, 72, 82},
            {30, 43, 63, 74, 85},
            {30, 45, 64, 75, 87},
            {31, 46, 66, 78, 90},
            {32, 47, 68, 80, 92},
            {33, 48, 70, 82, 94},
            {34, 50, 72, 85, 97},
            {35, 51, 74, 87, 99},
            {36, 53, 76, 89, 102},
            {37, 54, 78, 91, 105},
            {38, 55, 79, 93, 107},
            {39, 56, 81, 96, 110}
    };

    // 체지방률
    public static int[][] getBfpValueTable(String continent, boolean isMale) {
        switch (continent) {
            case Continents.CONTINENT_NORTH_AMERICA:
            case Continents.CONTINENT_SOUTH_AMERICA:
            default:
                return isMale ? BFP_MIN_VALUE_MALE_AMERICA_TABLE : BFP_MIN_VALUE_FEMALE_AMERICA_TABLE;
            case Continents.CONTINENT_ASIA:
            case Continents.CONTINENT_AFRICA:
                return isMale ? BFP_MIN_VALUE_MALE_ASIAN_TABLE : BFP_MIN_VALUE_FEMALE_ASIAN_TABLE;
            case Continents.CONTINENT_EUROPE:
            case Continents.CONTINENT_ANTARCTICA:
            case Continents.CONTINENT_OCEANIA:
                return isMale ? BFP_MIN_VALUE_MALE_EUROPE_TABLE : BFP_MIN_VALUE_FEMALE_EUROPE_TABLE;
        }
    }

    public final static int[] BFP_MIN_AGE_TABLE = {5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 40, 60};
    public final static int[][] BFP_MIN_VALUE_MALE_AMERICA_TABLE = {
            {5, 13, 20, 24, 31},
            {5, 13, 21, 25, 32},
            {6, 14, 21, 26, 33},
            {6, 14, 22, 27, 34},
            {6, 14, 23, 28, 35},
            {6, 14, 24, 29, 36},
            {6, 14, 24, 29, 36},
            {5, 13, 24, 29, 36},
            {5, 13, 23, 28, 35},
            {4, 12, 22, 27, 34},
            {3, 11, 22, 26, 33},
            {3, 11, 21, 25, 32},
            {3, 11, 21, 25, 32},
            {4, 12, 21, 26, 33},
            {3, 11, 21, 26, 33},
            {2, 10, 21, 27, 34},
            {4, 12, 23, 30, 37},
            {6, 14, 26, 31, 38}
    };
    public final static int[][] BFP_MIN_VALUE_FEMALE_AMERICA_TABLE = {
            {7, 15, 23, 27, 34},
            {7, 15, 24, 28, 35},
            {8, 16, 26, 30, 38},
            {8, 16, 27, 31, 39},
            {9, 17, 28, 32, 40},
            {9, 17, 29, 33, 41},
            {9, 17, 30, 34, 42},
            {9, 17, 30, 34, 42},
            {9, 17, 30, 34, 42},
            {9, 17, 31, 35, 43},
            {9, 17, 31, 35, 43},
            {9, 17, 31, 35, 43},
            {9, 17, 31, 36, 44},
            {9, 17, 31, 36, 44},
            {11, 19, 32, 37, 45},
            {13, 21, 33, 39, 47},
            {15, 23, 34, 40, 48},
            {16, 24, 36, 42, 50}
    };
    public final static int[][] BFP_MIN_VALUE_MALE_ASIAN_TABLE = {
            {1, 7, 25, 29, 37},
            {1, 7, 25, 29, 37},
            {1, 7, 25, 29, 37},
            {1, 7, 26, 29, 37},
            {1, 7, 26, 29, 37},
            {1, 7, 26, 29, 37},
            {1, 7, 26, 29, 37},
            {1, 7, 25, 29, 37},
            {1, 7, 25, 29, 37},
            {1, 7, 25, 28, 36},
            {1, 8, 24, 28, 36},
            {1, 8, 24, 27, 35},
            {1, 9, 23, 27, 35},
            {3, 11, 22, 27, 35},
            {3, 11, 22, 27, 35},
            {3, 11, 22, 27, 35},
            {4, 12, 23, 28, 36},
            {6, 14, 25, 30, 38}
    };
    public final static int[][] BFP_MIN_VALUE_FEMALE_ASIAN_TABLE = {
            {1, 8, 25, 29, 37},
            {1, 8, 25, 29, 37},
            {1, 9, 25, 30, 38},
            {2, 10, 26, 31, 39},
            {2, 10, 28, 32, 40},
            {3, 11, 29, 33, 41},
            {5, 13, 31, 35, 43},
            {6, 14, 32, 36, 45},
            {7, 15, 34, 38, 46},
            {9, 17, 35, 39, 47},
            {10, 18, 36, 40, 48},
            {11, 19, 37, 41, 49},
            {12, 20, 37, 41, 49},
            {13, 21, 35, 40, 48},
            {13, 21, 35, 40, 48},
            {13, 21, 35, 40, 48},
            {14, 22, 36, 41, 49},
            {15, 23, 37, 42, 50}
    };
    public final static int[][] BFP_MIN_VALUE_MALE_EUROPE_TABLE = {
            {5, 13, 22, 26, 34},
            {6, 14, 23, 26, 34},
            {6, 14, 24, 27, 35},
            {6, 14, 25, 28, 36},
            {6, 14, 26, 28, 36},
            {5, 13, 25, 29, 37},
            {5, 13, 25, 30, 38},
            {4, 12, 24, 29, 37},
            {4, 12, 23, 28, 36},
            {3, 11, 22, 28, 36},
            {3, 11, 21, 26, 34},
            {3, 11, 21, 26, 34},
            {3, 11, 21, 25, 33},
            {1, 8, 21, 26, 34},
            {1, 8, 21, 26, 34},
            {1, 8, 21, 26, 34},
            {3, 11, 23, 29, 37},
            {5, 13, 26, 31, 39}
    };
    public final static int[][] BFP_MIN_VALUE_FEMALE_EUROPE_TABLE = {
            {8, 16, 25, 29, 37},
            {8, 16, 26, 31, 39},
            {8, 16, 27, 31, 39},
            {9, 17, 28, 32, 40},
            {9, 17, 29, 33, 41},
            {9, 17, 29, 34, 42},
            {9, 17, 30, 35, 43},
            {9, 17, 30, 36, 44},
            {9, 17, 30, 36, 44},
            {9, 17, 31, 36, 44},
            {9, 17, 31, 36, 44},
            {9, 17, 31, 36, 44},
            {9, 17, 32, 37, 45},
            {12, 20, 33, 38, 46},
            {13, 21, 34, 39, 47},
            {14, 22, 34, 40, 48},
            {16, 24, 35, 41, 49},
            {17, 25, 37, 42, 50}
    };

    // BMI
    public static int[] getBmiHeightTable(String continent) {
        switch (continent) {
            case Continents.CONTINENT_NORTH_AMERICA:
            case Continents.CONTINENT_SOUTH_AMERICA:
            default:
                return BMI_MIN_HEIGHT_AMERICA_TABLE;
            case Continents.CONTINENT_ASIA:
                return BMI_MIN_HEIGHT_ASIA_TABLE;
            case Continents.CONTINENT_AFRICA:
                return BMI_MIN_HEIGHT_AFRICA_TABLE;
            case Continents.CONTINENT_EUROPE:
            case Continents.CONTINENT_ANTARCTICA:
            case Continents.CONTINENT_OCEANIA:
                return BMI_MIN_HEIGHT_EUROPE_TABLE;
        }
    }

    public static int[][] getBmiValueTable(String continent) {
        switch (continent) {
            case Continents.CONTINENT_NORTH_AMERICA:
            case Continents.CONTINENT_SOUTH_AMERICA:
            default:
                return BMI_MIN_VALUE_AMERICA_TABLE;
            case Continents.CONTINENT_ASIA:
                return BMI_MIN_VALUE_ASIA_TABLE;
            case Continents.CONTINENT_AFRICA:
                return BMI_MIN_VALUE_AFRICA_TABLE;
            case Continents.CONTINENT_EUROPE:
            case Continents.CONTINENT_ANTARCTICA:
            case Continents.CONTINENT_OCEANIA:
                return BMI_MIN_VALUE_EUROPE_TABLE;
        }
    }

    public final static int[] BMI_MIN_HEIGHT_AMERICA_TABLE = {125, 152, 155, 158, 161, 164, 167, 170, 173, 176, 179, 182, 185, 188, 192, 195, 198, 213, 228};
    public final static int[] BMI_MIN_HEIGHT_ASIA_TABLE = {125, 152, 155, 158, 161, 164, 167, 170, 173, 176, 179, 182, 185, 188, 192, 195, 198, 213, 228};
    public final static int[] BMI_MIN_HEIGHT_AFRICA_TABLE = {125, 152, 154, 156, 158, 160, 162, 164, 166, 168, 170, 172, 174, 176, 178, 180, 182, 184, 186, 188, 190, 192, 194};
    public final static int[] BMI_MIN_HEIGHT_EUROPE_TABLE = {140, 145, 150, 155, 160, 165, 170, 175, 180, 185, 190, 195, 200, 205, 210, 215};
    public final static int[][] BMI_MIN_VALUE_AMERICA_TABLE = {
            {15, 20, 25, 30, 36},
            {14, 19, 25, 30, 36},
            {14, 19, 25, 30, 36},
            {14, 19, 25, 30, 36},
            {14, 19, 25, 30, 36},
            {14, 19, 25, 30, 35},
            {14, 19, 25, 30, 35},
            {14, 19, 25, 30, 35},
            {15, 19, 25, 30, 35},
            {15, 19, 25, 30, 35},
            {15, 19, 25, 30, 35},
            {15, 19, 25, 30, 34},
            {15, 19, 25, 30, 35},
            {15, 19, 25, 30, 35},
            {15, 19, 25, 30, 34},
            {14, 19, 25, 30, 34},
            {14, 19, 25, 30, 35},
            {14, 19, 25, 30, 34},
            {14, 19, 25, 30, 34}
    };
    public final static int[][] BMI_MIN_VALUE_ASIA_TABLE = {
            {15, 20, 25, 30, 35},
            {14, 19, 25, 30, 35},
            {14, 18, 25, 30, 35},
            {14, 19, 25, 30, 34},
            {13, 18, 25, 30, 34},
            {14, 18, 25, 30, 34},
            {14, 19, 25, 30, 34},
            {14, 18, 25, 30, 35},
            {14, 18, 25, 30, 35},
            {14, 19, 25, 30, 34},
            {14, 18, 25, 30, 34},
            {15, 18, 25, 30, 34},
            {15, 18, 25, 30, 34},
            {14, 19, 25, 30, 33},
            {15, 18, 25, 29, 33},
            {14, 18, 25, 29, 34},
            {15, 18, 25, 29, 34},
            {14, 18, 25, 29, 34},
            {15, 19, 25, 29, 34}
    };
    public final static int[][] BMI_MIN_VALUE_AFRICA_TABLE = {
            {15, 19, 26, 30, 37},
            {15, 19, 25, 30, 36},
            {15, 19, 25, 30, 36},
            {15, 19, 25, 30, 36},
            {15, 19, 26, 30, 36},
            {15, 19, 26, 30, 36},
            {15, 19, 25, 30, 35},
            {15, 19, 25, 30, 35},
            {14, 19, 25, 30, 35},
            {14, 19, 26, 30, 35},
            {14, 19, 26, 30, 35},
            {14, 19, 25, 30, 35},
            {14, 19, 25, 30, 35},
            {14, 19, 25, 30, 35},
            {14, 19, 25, 30, 35},
            {14, 19, 25, 30, 34},
            {15, 19, 25, 30, 34},
            {15, 19, 25, 30, 34},
            {15, 19, 25, 30, 34},
            {15, 19, 25, 31, 34},
            {15, 19, 25, 30, 34},
            {15, 19, 25, 31, 34},
            {15, 19, 26, 30, 33}
    };
    public final static int[][] BMI_MIN_VALUE_EUROPE_TABLE = {
            {15, 20, 26, 31, 42},
            {15, 19, 26, 31, 41},
            {15, 19, 27, 31, 41},
            {15, 19, 27, 31, 41},
            {15, 20, 25, 31, 40},
            {14, 20, 26, 31, 40},
            {14, 19, 26, 31, 39},
            {14, 20, 26, 31, 39},
            {14, 19, 26, 31, 38},
            {13, 19, 26, 31, 38},
            {12, 19, 26, 30, 36},
            {12, 20, 26, 30, 36},
            {11, 19, 25, 30, 35},
            {12, 19, 26, 31, 36},
            {12, 19, 26, 31, 36},
            {13, 19, 26, 31, 36}
    };

    // Excel file 최소 적정 최대만 산재되어 있어, 적정과 최대 사이에 임의이 데이터 추가하여 정의
    public final static int[] PRO_AGE_TABLE = {39, 40, 60};
    public final static int[][] PRO_RATIO_VALUE_MALE_TABLE = {
            {10, 15, 21, 25, 30},    // 39 <=
            {10, 15, 21, 25, 30},    // 40 ~ 60
            {10, 15, 19, 23, 30},    // 60 >
    };
    public final static int[][] PRO_RATIO_VALUE_FEMALE_TABLE = {
            {5, 14, 18, 24, 30},    // 39 <=
            {5, 14, 18, 24, 30},    // 40 ~ 60
            {5, 13, 17, 23, 30},    // 60 >
    };
    public final static int[] MINERAL_AGE_TABLE = {39, 40, 60};
    public final static int[][] MINERAL_RATIO_VALUE_MALE_TABLE = {
            {1, 4, 6, 8, 10},   // 39 <=
            {1, 4, 6, 8, 10},   // 40 ~ 60
            {1, 4, 6, 8, 10},   // 60 >
    };
    public final static int[][] MINERAL_RATIO_VALUE_FEMALE_TABLE = {
            {1, 3, 5, 7, 10},   // 39 <=
            {1, 3, 5, 7, 10},   // 40 ~ 60
            {1, 3, 5, 7, 10},   // 60 >
    };
    // 세포외수분비 비율(남성/여성) => 실수 -> 정수 -> 실수
    //  표준이하	0~0.35 / 정상	0.36~0.39 /	주의(부종 초기)	0.40~0.44 / 높음(부종 발생 가능성 높음)	0.45
    public final static int[] ECF_VALUE_TABLE = {0 ,36, 39, 44, 45};

    // 표준근육량 Table define 2024.06.20    나이와 상관없이 동일.
    public final static int[] SMM_VALUE_MALE_TABLE = {0, 45, 55, 61, 66};
    public final static int[] SMM_VALUE_FEMALE_TABLE = {0, 35, 45, 50, 55};

    // 체지방률 범위
    public final static int[] BFP_AGE_TABLE = {39, 40, 60};
    public final static int[][] BFP_VALUE_MALE_TABLE = {
            {0, 8, 20, 35, 50},
            {0, 11, 21, 35, 50},
            {0, 13, 24, 35, 50},
    };
    public final static int[][] BFP_VALUE_FEMALE_TABLE = {
            {0, 21, 32, 41, 50},
            {0, 23, 33, 42, 50},
            {0, 24, 35, 43, 50},
    };

    // 표준 기초대사량 범위
    public final static int[] BMR_AGE_TABLE = {39, 40, 60};
    public final static int[][] BMR_VALUE_MALE_TABLE = {
            {800, 1600, 2400, 2700, 3000},
            {700, 1500, 2200, 2600, 2900},
            {600, 1400, 2000, 2300, 2600},
    };
    public final static int[][] BMR_VALUE_FEMALE_TABLE = {
            {800, 1600, 2000, 2500, 3000},
            {700, 1300, 1800, 2400, 3000},
            {600, 1200, 1600, 2300, 3000},
    };

    // Body Water Ratio Range
    public final static int[] BW_AGE_TABLE = {39, 40, 60};
    public final static int[][] BW_RATIO_VALUE_MALE_TABLE = {
            {39, 55, 65, 68, 70},
            {39, 55, 65, 68, 70},
            {39, 50, 60, 65, 70},
    };
    public final static int[][] BW_RATIO_VALUE_FEMALE_TABLE = {
            {29, 50, 60, 65, 70},
            {29, 50, 60, 65, 70},
            {29, 45, 55, 63, 70},
    };

}