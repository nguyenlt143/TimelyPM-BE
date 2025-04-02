package com.CapstoneProject.capstone.constant;

public class UrlConstant {

    public static final String API = "/api";
    public static class USER {
        public static final String USER = API + "/user";
        public static final String REGISTER = "/register";
        public static final String LOGIN = "/auth";
        public static final String GET = "";
        public static final String GET_ALL = "/get-all";
        public static final String GET_BY_ID = "/{id}";
        public static final String DELETE = "/{id}";
    }

    public static class PROJECT {
        public static final String PROJECT = API + "/project";
        public static final String CREATE = "/create";
        public static final String GET_PROJECT = "/{id}";
        public static final String GET_PROJECTS = "";
        public static final String GET_PROJECTS_BY_USER = "/user";
        public static final String DELETE_PROJECT = "/{id}";
        public static final String UPDATE_PROJECT = "/{id}";
        public static final String INVITE_PROJECT = "/{id}/invite";
        public static final String DELETE_MEMBER = "/{id}/delete/member";
        public static final String CLOSE_PROJECT = "/{id}/close";
    }

    public static class TOPIC {
        public static final String TOPIC = API + "/topic";
        public static final String CREATE = "/create";
        public static final String GET_TOPICS = "";
        public static final String GET_TOPIC = "/{id}";
        public static final String UPDATE_TOPIC = "/{id}";
        public static final String DELETE_TOPIC = "/{id}";
    }

    public static class TASK {
        public static final String TASK = API + "/task";
        public static final String CREATE = "/create";
        public static final String GET_TASKS = "";
        public static final String GET_TASK = "/{id}";
        public static final String UPDATE_TASKS = "/{id}";
        public static final String DELETE_TASKS = "/{id}";
        public static final String CREATE_ISSUE_BY_TASK = "/{id}/issue";
    }

    public static class ISSUE {
        public static final String ISSUE = API + "/issue";
        public static final String CREATE = "/create";
        public static final String GET_ISSUES = "";
        public static final String GET_ISSUE_BY_TASK = "/{id}/task";
        public static final String UPDATE_ISSUE = "/{id}";
        public static final String DELETE_ISSUE = "/{id}";
    }

    public static class QUESTION {
        public static final String QUESTION = API + "/question";
        public static final String CREATE = "/create";
        public static final String GET_QUESTIONS = "";
        public static final String GET_QUESTION = "/{id}";
    }

    public static class MEMBER {
        public static final String MEMBER = API + "/member";
        public static final String GET_MEMBERS = "";
    }

    public static class FILE {
        public static final String FILE = API + "/file";
        public static final String Add_FILE_IN_TASK = "/{id}/task";
        public static final String Add_FILE_IN_ISSUE = "/{id}/issue";
    }

}
