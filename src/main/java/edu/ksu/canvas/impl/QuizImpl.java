package edu.ksu.canvas.impl;

import com.google.gson.reflect.TypeToken;
import edu.ksu.canvas.interfaces.QuizReader;
import edu.ksu.canvas.interfaces.QuizWriter;
import edu.ksu.canvas.model.quizzes.Quiz;
import edu.ksu.canvas.net.Response;
import edu.ksu.canvas.net.RestClient;
import edu.ksu.canvas.util.CanvasURLBuilder;
import edu.ksu.canvas.exception.MessageUndeliverableException;
import edu.ksu.canvas.exception.OauthTokenRequiredException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QuizImpl extends  BaseImpl implements QuizReader, QuizWriter {
    private static final Logger LOG = Logger.getLogger(QuizReader.class);

    public QuizImpl(String canvasBaseUrl, Integer apiVersion, String oauthToken, RestClient restClient) {
        super(canvasBaseUrl, apiVersion, oauthToken, restClient);
    }

    @Override
    public Optional<Quiz> getSingleQuiz(String oauthToken, String courseId, String quizId) throws OauthTokenRequiredException, IOException {
        String url = CanvasURLBuilder.buildCanvasUrl(canvasBaseUrl, apiVersion,
                "courses/" + courseId + "/quizzes/" + quizId, Collections.emptyMap());
        Response response = canvasMessenger.getSingleResponseFromCanvas(oauthToken, url);
        return responseParser.parseToObject(Quiz.class, response);
    }

    @Override
    public List<Quiz> getQuizzesInCourse(String oauthToken, String courseId) throws OauthTokenRequiredException, IOException {
        String url = CanvasURLBuilder.buildCanvasUrl(canvasBaseUrl, apiVersion,
                "courses/" + courseId + "/quizzes", Collections.emptyMap());
        List<Response> responses = canvasMessenger.getFromCanvas(oauthToken, url);
        return parseQuizList(responses);
    }

    @Override
    public Optional<Quiz> updateQuiz(String oauthToken, Quiz quiz, String courseId) throws MessageUndeliverableException, IOException, OauthTokenRequiredException {
        String url = CanvasURLBuilder.buildCanvasUrl(canvasBaseUrl, apiVersion,
                "courses/" + courseId + "/quizzes/" + quiz.getId(), Collections.emptyMap());
        Response response = canvasMessenger.sendToJsonCanvas(oauthToken, url,
                getDefaultGsonParser().toJsonTree(quiz).getAsJsonObject());
        return responseParser.parseToObject(Quiz.class, response);
    }

    private List <Quiz> parseQuizList(final List<Response> responses) {
        return responses.stream().
                map(this::parseQuizList).
                flatMap(Collection::stream).
                collect(Collectors.toList());
    }

    private List<Quiz> parseQuizList(final Response response) {
        Type listType = new TypeToken<List<Quiz>>(){}.getType();
        return getDefaultGsonParser().fromJson(response.getContent(), listType);
    }

    @Override
    protected List parseListResponse(Response response) {
        return null;
    }

    @Override
    protected Optional parseObjectResponse(Response response) {
        return null;
    }
}
