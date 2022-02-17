package de.intelligence.drp.api;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public final class SecretGenerator {

    private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public static UUIDSecretGenerator uuidSecretGenerator() {
        return new UUIDSecretGenerator();
    }

    public static AlphabeticSecretGenerator alphabeticSecretGenerator(int size) {
        return new AlphabeticSecretGenerator(size);
    }

    public static Base64SecretGenerator base64SecretGenerator(int size) {
        return new Base64SecretGenerator(size);
    }

    public interface ISecretGenerator<T> {

        T generate();

    }

    public static final class UUIDSecretGenerator implements ISecretGenerator<UUID> {

        @Override
        public UUID generate() {
            return UUID.randomUUID();
        }

    }

    public static final class AlphabeticSecretGenerator implements ISecretGenerator<String> {

        private final int size;
        private final SecureRandom random;

        public AlphabeticSecretGenerator(int size) {
            this.size = size;
            this.random = new SecureRandom();
        }

        @Override
        public String generate() {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < this.size; i++) {
                builder.append(SecretGenerator.ALPHABET[this.random.nextInt(SecretGenerator.ALPHABET.length)]);
            }
            return builder.toString();
        }

    }

    public static final class Base64SecretGenerator implements ISecretGenerator<String> {

        private final int size;
        private final SecureRandom random;

        public Base64SecretGenerator(int size) {
            this.size = size;
            this.random = new SecureRandom();
        }

        @Override
        public String generate() {
            final byte[] byteBuf = new byte[size];
            this.random.nextBytes(byteBuf);
            return Base64.getEncoder().encodeToString(byteBuf);
        }

    }

}
