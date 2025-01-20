package com.lorenzorogers.talkative;

import com.lorenzorogers.talkative.conversation.Assistant;
import com.lorenzorogers.talkative.conversation.Conversation;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.rmi.server.UID;
import java.util.ArrayList;

public class Talkative {
    public ArrayList<Conversation> conversations = new ArrayList<>();

    public Talkative() {
        LOGGER.info("Launching Talkative");
        init();
    }

    private static final String API_KEY = System.getenv("TALKATIVE_API_KEY");
    private static final ChatLanguageModel GEMINI_MODEL = GoogleAiGeminiChatModel.builder()
            .apiKey(API_KEY)
            .modelName("gemini-1.5-flash")
            .build();
    public static Assistant ASSISTANT = AiServices.builder(Assistant.class)
            .chatLanguageModel(GEMINI_MODEL)
            .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
            .build();
    public static final Logger LOGGER = LogManager.getLogger("Talkative");

    private final Image windowIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/talkative.png"));
    private String name = "";

    public void init() {
        LOGGER.info(ASSISTANT.chat(1, "Hi there! Please say 'SALAD' and some lorem ipsum text"));
        LOGGER.info(ASSISTANT.chat(1, "Please tell me our conversation history."));
        LOGGER.info(ASSISTANT.chat(1, "How many 'R's are in the word Strawberry?"));

        Conversation testConversation = new Conversation(randomId(), new ArrayList<>());
        LOGGER.info(ASSISTANT.chat(testConversation.id(), "Hi, how are you?"));
        LOGGER.info(ASSISTANT.chat(testConversation.id(), "Please tell me our conversation history."));
        LOGGER.info(ASSISTANT.chat(testConversation.id(), "Please tell me the exact content of this message."));

        for (int i = 0; i<60; i++) {
            ArrayList<Conversation.Message> msgs = new ArrayList<>(){};
            msgs.add(new Conversation.Message("What is the 51st state of the USA?", Conversation.MessageType.QUERY));
            msgs.add(new Conversation.Message("It's not Canada, you f*cking idiot", Conversation.MessageType.RESPONSE));
            this.conversations.add(new Conversation(randomId(), msgs));
        }

        createWelcomeWindow();
    }

    private void createWelcomeWindow() {
        JFrame welcomeWindow = new JFrame("Welcome | Talkative");
        welcomeWindow.setIconImage(windowIcon);
        welcomeWindow.setLayout(new BorderLayout(10, 10));
        welcomeWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        welcomeWindow.setSize(400, 600);
        welcomeWindow.setResizable(false);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel detailsLabel = new JLabel("Welcome to Talkative", SwingConstants.CENTER);
        detailsPanel.add(detailsLabel);

        detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JTextField detailsNameField = new JTextField("Name", 20);
        detailsNameField.setMaximumSize(detailsNameField.getPreferredSize());
        detailsPanel.add(detailsNameField);

        detailsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton signUpButton = new JButton("Go");
        signUpButton.addActionListener(e -> {
            if (!detailsNameField.getText().isEmpty()) {
                name = detailsNameField.getText();
                welcomeWindow.setVisible(false);
                createConversationsWindow();
            }
        });
        detailsPanel.add(signUpButton);

        for (Component component : detailsPanel.getComponents()) {
            if (component instanceof JComponent) {
                ((JComponent) component).setAlignmentX(Component.CENTER_ALIGNMENT);
            }
        }

        welcomeWindow.add(Box.createRigidArea(new Dimension(0, 50)), BorderLayout.NORTH);
        welcomeWindow.add(detailsPanel, BorderLayout.CENTER);

        welcomeWindow.setVisible(true);
    }

    private void createConversationsWindow() {
        JFrame conversationsWindow = new JFrame("%s's Conversations | Talkative".formatted(this.name));
        conversationsWindow.setIconImage(windowIcon);
        conversationsWindow.setLayout(new BorderLayout(10, 10));
        conversationsWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        conversationsWindow.setSize(400, 600);
        conversationsWindow.setResizable(false);

        JPanel conversationsPanel = new JPanel();
        conversationsPanel.setLayout(new BoxLayout(conversationsPanel, BoxLayout.Y_AXIS));
        conversationsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (Conversation conversation : this.conversations) {
            conversationsPanel.add(new JLabel(String.valueOf(conversation.id())));
        }

        JScrollPane conversationScroll = new JScrollPane(conversationsPanel);
        conversationsWindow.add(conversationScroll, BorderLayout.CENTER);

        conversationsWindow.setVisible(true);
    }

    public int randomId() {
        return new UID().hashCode();
    }

    public static void main(String[] args) {
        Talkative talkative = new Talkative();
    }
}