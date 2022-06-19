package main.service;

import main.model.FoundPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class FoundPageService {

    private ArrayList<FoundPage> foundPages;

    @Autowired
    public FoundPageService() {
        foundPages = new ArrayList<>();
    }

    public ArrayList<FoundPage> getFoundPages() {
        foundPages.sort(FoundPage::compareTo);
        return foundPages;
    }

    public void setFoundPages(FoundPage foundPage) {
        foundPages.add(foundPage);
    }

    public void clearFoundPages() {
        foundPages.clear();
    }
}
