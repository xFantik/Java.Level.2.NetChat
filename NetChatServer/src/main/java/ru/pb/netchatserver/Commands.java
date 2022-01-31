package ru.pb.netchatserver;

public class Commands {
    public static final String SET_NAME = "/name";
    public static final String SET_NAME_SUCCESS = "/acceptName";
    public static final String NAME_IS_DENY = "/denyName";
    public static final String NAME_IS_BUSY = "/busyName";
    public static final String GET_CONTACTS = "/list";
    public static final String NEW_NAME = "/new";



    public static final String MESSAGE_GROUP = "/all";
    public static final String MESSAGE_PRIVATE = "/pm";



    //Служебные строки для передачи списка контактов
    public static final String DELIMITER_START_ENTRY  = "&i:";
    public static final String DELIMITER_START_NAME  = "&n:";
}

