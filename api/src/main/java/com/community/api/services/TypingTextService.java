package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.Image;
import com.community.api.entity.TypingText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
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
}
