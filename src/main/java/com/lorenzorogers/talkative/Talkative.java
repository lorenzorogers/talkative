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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Objects;

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

    public static final Logger LOGGER = LogManager.getLogger("Talkative");

    private final Image windowIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/talkative.png"));
    private String name = "";
    public static Assistant ASSISTANT = AiServices.builder(Assistant.class)
            .chatLanguageModel(GEMINI_MODEL)
            .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
            .build();

    public void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException |
                 ClassNotFoundException |
                 InstantiationException |
                 IllegalAccessException e) {
            LOGGER.error("Failed to set program look+feel");
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

        JButton createButton = new JButton("Start Conversation");
        createButton.addActionListener(e -> {
            String startingMessage = JOptionPane.showInputDialog("Start a new conversation");
            if (startingMessage != null && !startingMessage.isEmpty()) {
                String conversationTitle = GEMINI_MODEL.generate("Please create a one to four word summary title based on a conversation starting with \"%s\". Please use sentence case with no ending punctuation.".formatted(startingMessage));
                Conversation conversation = new Conversation(randomId(), conversationTitle, new ArrayList<>());
                conversation.messages().add(new Conversation.Message(startingMessage, Conversation.MessageType.QUERY));
                conversation.messages().add(new Conversation.Message(ASSISTANT.chatWithName(this.name, conversation.id(), startingMessage), Conversation.MessageType.RESPONSE));
                conversations.add(conversation);
                createConversationWindow(conversation);
                conversationsWindow.setVisible(false);
            }
        });
        conversationsWindow.add(createButton, BorderLayout.NORTH);

        JPanel conversationsPanel = new JPanel();
        conversationsPanel.setLayout(new BoxLayout(conversationsPanel, BoxLayout.Y_AXIS));
        conversationsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (Conversation conversation : this.conversations.reversed()) {
            JLabel conversationLabel = new JLabel(conversation.title());
            conversationLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    createConversationWindow(conversation);
                    conversationsWindow.setVisible(false);
                    super.mouseClicked(e);
                }
            });
            conversationLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            conversationsPanel.add(conversationLabel);
        }

        JScrollPane conversationScroll = new JScrollPane(conversationsPanel);
        conversationsWindow.add(conversationScroll, BorderLayout.CENTER);

        conversationsWindow.setVisible(true);
    }

    private void createConversationWindow(Conversation conversation) {
        JFrame conversationWindow = new JFrame("%s | Talkative".formatted(conversation.title()));
        conversationWindow.setIconImage(windowIcon);
        conversationWindow.setLayout(new BorderLayout(10, 10));
        conversationWindow.setSize(400, 600);
        conversationWindow.setResizable(false);
        conversationWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Conversation preSave = conversations.stream().filter(iteratedConversation -> iteratedConversation.id() == conversation.id()).findFirst().orElse(null);
                conversations.set(conversations.indexOf(preSave), conversation);
                createConversationsWindow();
                super.windowClosing(e);
            }
        });

        JPanel messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (Conversation.Message message : conversation.messages()) {
            addMessage(message, messagesPanel);
        }

        JScrollPane messageScroll = new JScrollPane(messagesPanel);
        messageScroll.setHorizontalScrollBar(null);
        conversationWindow.add(messageScroll, BorderLayout.CENTER);

        JPanel bottomActionsPanel = new JPanel(new BorderLayout());

        JTextField messageInput = new JTextField(35);
        bottomActionsPanel.add(messageInput, BorderLayout.WEST);

        JButton sendMessageButton = new JButton("Send");
        sendMessageButton.addActionListener(e -> {
            if (!Objects.equals(messageInput.getText(), "")) {
                LOGGER.debug("Sending message of %s".formatted(sendMessageButton.getText()));

                Conversation.Message query = new Conversation.Message(messageInput.getText(), Conversation.MessageType.QUERY);
                conversation.messages().add(query);
                addMessage(query, messagesPanel);
                Conversation.Message response = new Conversation.Message(ASSISTANT.chat(conversation.id(), messageInput.getText()), Conversation.MessageType.RESPONSE);
                conversation.messages().add(response);
                addMessage(response, messagesPanel);

            }
        });
        bottomActionsPanel.add(sendMessageButton, BorderLayout.EAST);

        conversationWindow.add(bottomActionsPanel, BorderLayout.SOUTH);

        conversationWindow.setVisible(true);
    }

    private void addMessage(Conversation.Message message, JPanel panel) {
        JPanel messagePanel = new JPanel(new BorderLayout(10, 10));
        JLabel messageContext = new JLabel(message.type().getType());
        messagePanel.add(messageContext, BorderLayout.NORTH);
        JTextArea messageLabel = new JTextArea(message.content());
        messageLabel.setEditable(false);
        messageLabel.setLineWrap(true);
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        panel.add(messagePanel);
        panel.revalidate();
        panel.repaint();
    }

    public int randomId() {
        return new UID().hashCode();
    }

    public static void main(String[] args) {
        Talkative talkative = new Talkative();
    }
}