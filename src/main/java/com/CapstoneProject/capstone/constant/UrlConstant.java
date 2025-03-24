package com.CapstoneProject.capstone.constant;

public class UrlConstant {

    public static final String API = "/api";
    public static class USER {
        public static final String USER = API + "/user";
        public static final String REGISTER = "/register";
        public static final String LOGIN = "/auth";
        public static final String GET = "";
    }

    public static class PROJECT {
        public static final String PROJECT = API + "/project";
        public static final String CREATE = "/create";
        public static final String GET_PROJECT = "/{id}";
        public static final String GET_PROJECTS = "";
        public static final String DELETE_PROJECT = "/{id}";
        public static final String UPDATE_PROJECT = "/{id}";
        public static final String INVITE_PROJECT = "/{id}/invite";
        public static final String DELETE_MEMBER = "/{id}/delete/member";
    }

    public static class TOPIC {
        public static final String TOPIC = API + "/topic";
        public static final String CREATE = "/create";
        public static final String GET_TOPICS = "";
    }

    public static class TASK {
        public static final String TASK = API + "/task";
        public static final String CREATE = "/create";
        public static final String GET_TASKS = "";
    }

    public static class ISSUE {
        public static final String ISSUE = API + "/issue";
        public static final String CREATE = "/create";
        public static final String GET_ISSUES = "";
    }

    public static class QUESTION {
        public static final String QUESTION = API + "/question";
        public static final String CREATE = "/create";
        public static final String GET_QUESTIONS = "";
    }

    public static class MEMBER {
        public static final String MEMBER = API + "/member";
        public static final String GET_MEMBERS = "";
    }

}
