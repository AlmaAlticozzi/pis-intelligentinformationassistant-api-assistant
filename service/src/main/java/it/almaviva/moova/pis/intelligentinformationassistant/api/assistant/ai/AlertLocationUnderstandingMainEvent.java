package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

public record AlertLocationUnderstandingMainEvent(
        AlertLocationMainEventIntent eventIntent,
        double confidence) {

    public AlertLocationUnderstandingMainEvent {
        eventIntent = eventIntent == null ? AlertLocationMainEventIntent.UNKNOWN : eventIntent;
        confidence = clamp(confidence);
    }

    public static AlertLocationUnderstandingMainEvent unknown() {
        return new AlertLocationUnderstandingMainEvent(AlertLocationMainEventIntent.UNKNOWN, 0.0);
    }

    private static double clamp(double value) {
        if (Double.isNaN(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
