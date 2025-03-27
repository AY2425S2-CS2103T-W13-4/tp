package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * Helper functions for handling strings.
 */
public class StringUtil {

    /**
     * Returns true if the {@code sentence} contains the {@code word}.
     *   Ignores case, but a full word match is required.
     *   <br>examples:<pre>
     *       containsWordIgnoreCase("ABc def", "abc") == true
     *       containsWordIgnoreCase("ABc def", "DEF") == true
     *       containsWordIgnoreCase("ABc def", "AB") == false //not a full word match
     *       </pre>
     * @param sentence cannot be null
     * @param word cannot be null, cannot be empty, must be a single word
     */
    public static boolean containsWordIgnoreCase(String sentence, String word) {
        requireNonNull(sentence);
        requireNonNull(word);

        String preppedWord = word.trim();
        checkArgument(!preppedWord.isEmpty(), "Word parameter cannot be empty");
        checkArgument(preppedWord.split("\\s+").length == 1, "Word parameter should be a single word");

        String preppedSentence = sentence;
        String[] wordsInPreppedSentence = preppedSentence.split("\\s+");

        return Arrays.stream(wordsInPreppedSentence)
                .anyMatch(preppedWord::equalsIgnoreCase);
    }

    /**
     * Returns a detailed message of the t, including the stack trace.
     */
    public static String getDetails(Throwable t) {
        requireNonNull(t);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return t.getMessage() + "\n" + sw.toString();
    }

    /**
     * Returns true if {@code s} represents a non-zero unsigned integer
     * e.g. 1, 2, 3, ..., {@code Integer.MAX_VALUE} <br>
     * Will return false for any other non-null string input
     * e.g. empty string, "-1", "0", "+1", and " 2 " (untrimmed), "3 0" (contains whitespace), "1 a" (contains letters)
     * @throws NullPointerException if {@code s} is null.
     */
    public static boolean isNonZeroUnsignedInteger(String s) {
        requireNonNull(s);

        try {
            int value = Integer.parseInt(s);
            return value > 0 && !s.startsWith("+"); // "+1" is successfully parsed by Integer#parseInt(String)
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Builds the 2D dynamic programming array to compute the edit distance between two words
     * @param w1 The first word
     * @param w2 The second word
     * @return The fully filled 2D table
     */
    private static int[][] buildEditDistanceTable(String w1, String w2) {
        int[][] dp = new int[w1.length() + 1][w2.length() + 1];

        // Base cases, if one string is empty, return the length of the other string
        for (int i = 0; i < w1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j < w2.length(); j++) {
            dp[0][j] = j;
        }

        // Build the table bottom-up
        for (int i = 1; i <= w1.length(); i++) {
            for (int j = 1; j <= w2.length(); j++) {
                if (w1.charAt(i - 1) == w2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int insertOp = dp[i][j - 1] + 1;
                    int deleteOp = dp[i - 1][j] + 1;
                    int replaceOp = dp[i - 1][j - 1] + 1;

                    dp[i][j] = Math.min(insertOp, Math.min(deleteOp, replaceOp));
                }
            }
        }

        return dp;
    }

    /**
     * Returns the edit distance between two words.<br><br>
     *
     * The <b>edit distance</b> between two words is the smallest number of
     * operations on the first word to get to the second word. The operations include:<br>
     * <ul>
     *     <li>Replace a letter of the original word with another letter.</li>
     *     <li>Remove a letter from the original word.</li>
     *     <li>Add a letter to the original word.</li>
     * </ul>
     *
     * @param word1 The first word
     * @param word2 The second word
     * @return The edit distance between two words
     */
    private static double getEditDistance(String word1, String word2) {
        word1 = " " + word1;
        word2 = " " + word2;

        int[][] dp = buildEditDistanceTable(word1, word2);

        return dp[word1.length()][word2.length()];
    }

    /**
     * Computes the closeness of a String to the user's keyword.<br>
     * This returns 0 if the keyword is contained within the String.<br>
     * Else, split the keywords and the String into two String arrays and perform matching for each word
     * in the keywords, sum the edit distances for each keyword, and return this value.
     *
     * @param stringToCompareTo The String to compare the keywords to
     * @param keyword The user's keyword(s), which can be one word or multiple words separated by whitespace
     * @return The closeness of the task's description to the user's keyword
     */
    public static double computeCloseness(String stringToCompareTo, String keyword) {
        String[] keywordWords = keyword.split("\\s+");
        String[] descriptionWords = stringToCompareTo.split("\\s+");

        double distance = 0;

        // for each keyword word, compute the smallest edit distance to any of the description word
        for (String kw : keywordWords) {
            double minDistanceToKw = Double.MAX_VALUE;

            for (String dw : descriptionWords) {
                if (dw.contains(kw)) {
                    minDistanceToKw = 0;
                    break;
                }

                minDistanceToKw = Math.min(minDistanceToKw, getEditDistance(dw, kw));
            }

            // sum the smallest edit distances for each keyword word up
            distance += minDistanceToKw;
        }

        return distance;
    }
}
