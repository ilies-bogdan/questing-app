package utils;

public class RandomString {
    /**
     * Gets a random aphanumerical string.
     * @param size - the size of the random string
     * @return a randomly generated alphanumerical string of the given size
     */
    static public String getRandomString(int size) {
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < size; i++) {
            int index = (int) (Constants.ALPHA_NUMERIC_STRING.length() * Math.random());
            randomString.append(Constants.ALPHA_NUMERIC_STRING.charAt(index));
        }
        return randomString.toString();
    }
}