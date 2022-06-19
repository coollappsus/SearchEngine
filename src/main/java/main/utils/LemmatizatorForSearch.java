package main.utils;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

public class LemmatizatorForSearch {
    private final String CONJUNCTION_RU = "СОЮЗ";
    private final String PREPOSITION_RU = "ПРЕДЛ";
    private final String INTERJECTION_RU = "МЕЖД";
    private final String PRONOUN = "ПРЕДК";
    private final String ARTICLE = "ARTICLE";
    private final String PREPOSITION_EN = "PREP";
    private final String CONJUNCTION_EN = "CONJ";
    private final String INTERJECTION_EN = "INT";
    private LuceneMorphology luceneMorphRu = new RussianLuceneMorphology();
    private LuceneMorphology luceneMorphEn = new EnglishLuceneMorphology();
    private String line;

    public LemmatizatorForSearch(String line) throws IOException {
        this.line = line;
    }

    public TreeMap<String, String> runAndPrepare() {
        List<String> words = List.of(line.split("[\\d\\s+()?!/:.,\";-]"));
        return checkLemma(words);
    }

    public TreeMap<String, String> checkLemma(List<String> words) {
        TreeMap<String, String> lemmaList = new TreeMap<>();
        for (String word : words) {
            if (word.length() < 2) continue;
            if (luceneMorphRu.checkString(word)) {
                List<String> wordBaseForms = luceneMorphRu.getNormalForms(word);
                if (checkSecondaryWordRu(wordBaseForms.get(0))) continue;
                lemmaList.put(wordBaseForms.get(0), word);
            } else if (luceneMorphEn.checkString(word)){
                List<String> wordBaseForms = luceneMorphEn.getNormalForms(word);
                if (checkSecondaryWordEn(wordBaseForms.get(0))) continue;
                lemmaList.put(wordBaseForms.get(0), word);
            }
        }
        return lemmaList;
    }

    public boolean checkSecondaryWordEn(String str) {
        List<String> wordBaseForms = luceneMorphEn.getMorphInfo(str);
        return wordBaseForms.get(0).contains(ARTICLE) || wordBaseForms.get(0).contains(PREPOSITION_EN) ||
                wordBaseForms.get(0).contains(INTERJECTION_EN) || wordBaseForms.get(0).contains(CONJUNCTION_EN);
    }

    public boolean checkSecondaryWordRu(String str) {
        List<String> wordBaseForms = luceneMorphRu.getMorphInfo(str);
        return wordBaseForms.get(0).contains(CONJUNCTION_RU) || wordBaseForms.get(0).contains(PREPOSITION_RU) ||
                wordBaseForms.get(0).contains(INTERJECTION_RU) || wordBaseForms.get(0).contains(PRONOUN);
    }
}
