package utils;

public class RandomString {
    static public String getRandomString(int size) {
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < size; i++) {
            int index = (int) (Constants.ALPHA_NUMERIC_STRINGS.length() * Math.random());
            randomString.append(Constants.ALPHA_NUMERIC_STRINGS.charAt(index));
        }
        return randomString.toString();
    }
}