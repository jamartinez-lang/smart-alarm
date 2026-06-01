package com.smartalarm.model;

import java.util.Random;

public class MathChallenge {

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    private final Difficulty difficulty;
    private final String question;
    private final int answer;
    private boolean solved;

    private static final Random RANDOM = new Random();

    public MathChallenge(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.solved = false;
        int[] generated = generateChallenge(difficulty);
        this.answer = generated[0];
        this.question = buildQuestion(generated, difficulty);
    }

    private int[] generateChallenge(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> {
                int a = RANDOM.nextInt(10) + 1;
                int b = RANDOM.nextInt(10) + 1;
                yield new int[]{a + b, a, b, 0};
            }
            case MEDIUM -> {
                int a = RANDOM.nextInt(15) + 5;
                int b = RANDOM.nextInt(10) + 2;
                yield new int[]{a * b, a, b, 1};
            }
            case HARD -> {
                int a = RANDOM.nextInt(20) + 10;
                int b = RANDOM.nextInt(15) + 5;
                int c = RANDOM.nextInt(8) + 2;
                yield new int[]{a * b - c, a, b, c};
            }
        };
    }

    private String buildQuestion(int[] parts, Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> String.format("%d + %d = ?", parts[1], parts[2]);
            case MEDIUM -> String.format("%d × %d = ?", parts[1], parts[2]);
            case HARD -> String.format("(%d × %d) - %d = ?", parts[1], parts[2], parts[3]);
        };
    }

    public boolean attempt(int userAnswer) {
        if (userAnswer == answer) {
            solved = true;
            return true;
        }
        return false;
    }

    public String getQuestion() { return question; }
    public boolean isSolved() { return solved; }
    public Difficulty getDifficulty() { return difficulty; }

    @Override
    public String toString() {
        return String.format("MathChallenge{difficulty=%s, question='%s', solved=%s}",
                difficulty, question, solved);
    }
}