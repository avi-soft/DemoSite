package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.Image;
import com.community.api.entity.Qualification;
import com.community.api.entity.TypingText;
import com.community.api.utils.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class TypingTextService
{

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public List<TypingText> getAllRandomTypingTexts()
    {
        TypedQuery<TypingText> typedQuery= entityManager.createQuery(Constant.GET_ALL_RANDOM_TYPING_TEXT,TypingText.class);
        List<TypingText> typingTexts = typedQuery.getResultList();
        return typingTexts;
    }

    @Transactional
    public List<TypingText> addAllRandomTypingTexts(List<TypingText> typingTexts)
    {
        List<TypingText> typingTextsListToAdd = new ArrayList<>();
        for(TypingText typedText : typingTexts)
        {
            TypingText typingTextToAdd =new TypingText();
            long id = findCount() + 1;
            if (typedText.getText() == null || typedText.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Typing text cannot be empty or consist only of whitespace");
            }
            List<TypingText> existingTypingText = getAllRandomTypingTexts();
            for (TypingText existingTypingText1: existingTypingText) {
                if (existingTypingText1.getText().equalsIgnoreCase(typedText.getText())) {
                    throw new IllegalArgumentException("Typing Text with name '"+typedText.getText()+"' already exists");
                }
            }
            typingTextToAdd.setId(id);
            typingTextToAdd.setText(typedText.getText());
            typingTextsListToAdd.add(typingTextToAdd);
            entityManager.persist(typingTextToAdd);
        }
        return typingTextsListToAdd;
    }

    public long findCount() {
        String queryString = Constant.GET_TYPING_TEXT_COUNT;
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult();
    }
}
