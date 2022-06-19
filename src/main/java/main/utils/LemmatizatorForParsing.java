package main.utils;

import main.model.Lemma;
import main.service.LemmaService;
import main.service.SiteService;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class LemmatizatorForParsing implements LemmaInterface {
    private final LemmaService lemmaService;
    private final SiteService siteService;
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
    private String url;

    public LemmatizatorForParsing(LemmaService lemmaService,
                                  SiteService siteService, String line, String url) throws IOException {
        this.lemmaService = lemmaService;
        this.siteService = siteService;
        this.line = line.toLowerCase(Locale.ROOT);
        this.url = url;
    }

    @Override
    public TreeMap<String, Float> runAndPrepare() {
        List<String> words = List.of(line.split("[\\d\\s+()?!/:.,\";-]"));
        return checkLemma(words);
    }

    @Override
    public TreeMap<String, Float> checkLemma(List<String> words) {
        TreeMap<String, Float> lemmaList = new TreeMap<>();
        for (String word : words) {
            if (word.length() < 2) continue;
            if (luceneMorphRu.checkString(word)) {
                List<String> wordBaseForms = luceneMorphRu.getNormalForms(word);
                if (checkSecondaryWordRu(wordBaseForms.get(0))) continue;
                workWithDataBase(wordBaseForms.get(0));
                lemmaList.merge(wordBaseForms.get(0), 1F, Float::sum);
            } else if (luceneMorphEn.checkString(word)){
                List<String> wordBaseForms = luceneMorphEn.getNormalForms(word);
                if (checkSecondaryWordEn(wordBaseForms.get(0))) continue;
                workWithDataBase(wordBaseForms.get(0));
                lemmaList.merge(wordBaseForms.get(0), 1F, Float::sum);
            }
        }
        return lemmaList;
    }

    @Override
    public boolean checkSecondaryWordEn(String str) {
        List<String> wordBaseForms = luceneMorphEn.getMorphInfo(str);
        return wordBaseForms.get(0).contains(ARTICLE) || wordBaseForms.get(0).contains(PREPOSITION_EN) ||
                wordBaseForms.get(0).contains(INTERJECTION_EN) || wordBaseForms.get(0).contains(CONJUNCTION_EN);
    }

    @Override
    public boolean checkSecondaryWordRu(String str) {
        List<String> wordBaseForms = luceneMorphRu.getMorphInfo(str);
        return wordBaseForms.get(0).contains(CONJUNCTION_RU) || wordBaseForms.get(0).contains(PREPOSITION_RU) ||
                wordBaseForms.get(0).contains(INTERJECTION_RU) || wordBaseForms.get(0).contains(PRONOUN);
    }

    private void workWithDataBase(String wordBaseForm) {
        if (lemmaService.findByLemma(wordBaseForm) == null) {
            Lemma lemma = new Lemma(wordBaseForm, 1, siteService.findByUrl(url));
            lemmaService.saveLemma(lemma);
        } else {
            lemmaService.updateLemma(lemmaService.findByLemma(wordBaseForm));
        }
    }
}
