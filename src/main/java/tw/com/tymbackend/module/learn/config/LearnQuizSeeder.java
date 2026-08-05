package tw.com.tymbackend.module.learn.config;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import tw.com.tymbackend.module.learn.dao.LearnOptionRepository;
import tw.com.tymbackend.module.learn.dao.LearnQuestionRepository;
import tw.com.tymbackend.module.learn.dao.LearnQuizRepository;
import tw.com.tymbackend.module.learn.domain.vo.LearnOption;
import tw.com.tymbackend.module.learn.domain.vo.LearnQuestion;
import tw.com.tymbackend.module.learn.domain.vo.LearnQuiz;

@Component
public class LearnQuizSeeder {
    private final ObjectMapper objectMapper;
    private final LearnQuizRepository quizzes;
    private final LearnQuestionRepository questions;
    private final LearnOptionRepository options;

    public LearnQuizSeeder(ObjectMapper objectMapper, LearnQuizRepository quizzes,
                           LearnQuestionRepository questions, LearnOptionRepository options) {
        this.objectMapper = objectMapper;
        this.quizzes = quizzes;
        this.questions = questions;
        this.options = options;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedBundledQuizzes() throws IOException {
        seed("learn/toeic-reading-mini-50-v1.json");
    }

    private void seed(String path) throws IOException {
        SeedQuiz seed = objectMapper.readValue(new ClassPathResource(path).getInputStream(), SeedQuiz.class);
        if (quizzes.existsById(seed.id())) return;

        LearnQuiz quiz = new LearnQuiz();
        quiz.setId(seed.id());
        quiz.setTitle(seed.title());
        quiz.setDescription(seed.description());
        quiz.setRecommendedMinutes(seed.recommendedMinutes());
        quiz.setPublished(true);
        quizzes.save(quiz);

        for (SeedQuestion item : seed.questions()) {
            LearnQuestion question = new LearnQuestion();
            question.setQuiz(quiz);
            question.setPosition(item.position());
            question.setSection(item.section());
            question.setPassageKey(item.passageKey());
            question.setPassageText(item.passageText());
            question.setPrompt(item.prompt());
            question.setCorrectOption(item.correctOption());
            question.setExplanation(item.explanation());
            questions.save(question);

            List<LearnOption> questionOptions = item.options().stream().map(itemOption -> {
                LearnOption option = new LearnOption();
                option.setQuestion(question);
                option.setKey(itemOption.key());
                option.setText(itemOption.text());
                return option;
            }).toList();
            options.saveAll(questionOptions);
        }
    }

    private record SeedQuiz(String id, String title, String description, Integer recommendedMinutes,
                            List<SeedQuestion> questions) {}
    private record SeedQuestion(Integer position, String section, String passageKey, String passageText,
                                String prompt, List<SeedOption> options, String correctOption,
                                String explanation) {}
    private record SeedOption(String key, String text) {}
}
