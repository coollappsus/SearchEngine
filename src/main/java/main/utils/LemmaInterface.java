package main.utils;

import java.util.List;
import java.util.TreeMap;

public interface LemmaInterface {

    TreeMap<String, Float> runAndPrepare();

    TreeMap<String, Float> checkLemma(List<String> words);

    boolean checkSecondaryWordEn(String str);

    boolean checkSecondaryWordRu(String str);
}
