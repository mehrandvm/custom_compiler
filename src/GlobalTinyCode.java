public class GlobalTinyCode {
    public static int regCounter = 4;
    public static String getNextReg() {
        return "r" + (regCounter++);
    }

}
