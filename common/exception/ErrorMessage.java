package grakn.client.common.exception;

public abstract class ErrorMessage extends grakn.common.exception.ErrorMessage {

    private ErrorMessage(String codePrefix, int codeNumber, String messagePrefix, String messageBody) {
        super(codePrefix, codeNumber, messagePrefix, messageBody);
    }

    public static class ClientInternal extends ErrorMessage {
        public static final ClientInternal ILLEGAL_STATE =
                new ClientInternal(1, "Illegal internal state!");
        public static final ClientInternal UNRECOGNISED_VALUE =
                new ClientInternal(2, "Unrecognised schema value!");
        public static final ClientInternal ILLEGAL_ARGUMENT_NULL =
                new ClientInternal(3, "'%s' can not be null.");
        public static final ClientInternal ILLEGAL_ARGUMENT_NULL_OR_EMPTY =
                new ClientInternal(4, "'%s' can not be null or empty.");

        private static final String codePrefix = "CIN";
        private static final String messagePrefix = "Invalid Internal Client State";

        ClientInternal(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Connection extends ErrorMessage {
        public static final Connection CONNECTION_CLOSED =
                new Connection(1, "The connection to the database is closed.");
        public static final Connection NEGATIVE_BATCH_SIZE =
                new Connection(2, "Batch size cannot be less than 1, was: '%d'.");
        public static final Connection INVALID_BATCH_SIZE_MODE =
                new Connection(3, "Invalid batch size mode: '%s'.");
        public static final Connection TRANSACTION_LISTENER_TERMINATED =
                new Connection(4, "Transaction listener was terminated");

        private static final String codePrefix = "CNN";
        private static final String messagePrefix = "Database Connection Error";

        Connection(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Concept extends ErrorMessage {
        public static final Concept UNRECOGNISED_CONCEPT =
                new Concept(1, "The %s '%s' was not recognised.");
        public static final Concept INVALID_CONCEPT_CASTING =
                new Concept(2, "Invalid concept conversion from '%s' to '%s'.");

        private static final String codePrefix = "CON";
        private static final String messagePrefix = "Concept Error";

        Concept(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Query extends ErrorMessage {
        public static final Query VARIABLE_DOES_NOT_EXIST =
                new Query(1, "The variable '%s' does not exist.");
        public static final Query NO_EXPLANATION =
                new Query(2, "No explanation was found.");
        public static final Query UNRECOGNISED_QUERY_OBJECT =
                new Query(3, "The query object '%s' was not recognised.");

        private static final String codePrefix = "QRY";
        private static final String messagePrefix = "Query Error";

        Query(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Protocol extends ErrorMessage {
        public static final Protocol UNRECOGNISED_FIELD =
                new Protocol(1, "The %s '%s' was not recognised.");
        public static final Protocol REQUIRED_FIELD_NOT_SET =
                new Protocol(2, "The required field '%s' was not set.");

        private static final String codePrefix = "PRO";
        private static final String messagePrefix = "Protocol Error";

        Protocol(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }
}
