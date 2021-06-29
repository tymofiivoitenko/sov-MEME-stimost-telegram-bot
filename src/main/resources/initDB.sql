DROP TABLE IF EXISTS java_quiz;
DROP TABLE IF EXISTS users;
CREATE SEQUENCE global_seq START WITH 100000;

CREATE TABLE users
(
    id         INTEGER PRIMARY KEY DEFAULT nextval('global_seq'),
    chat_id    INTEGER UNIQUE                NOT NULL,
    name       VARCHAR                       NOT NULL,
    score      INTEGER             DEFAULT 0 NOT NULL,
    high_score INTEGER             DEFAULT 0 NOT NULL,
    bot_state  VARCHAR                       NOT NULL
);

CREATE TABLE java_quiz
(
    id             INTEGER PRIMARY KEY DEFAULT nextval('global_seq'),
    question       VARCHAR NOT NULL,
    answer_correct VARCHAR NOT NULL,
    option1        VARCHAR NOT NULL,
    option2        VARCHAR NOT NULL,
    option3        VARCHAR NOT NULL
);

DELETE
FROM java_quiz;

INSERT INTO java_quiz (question, answer_correct, option1, option2, option3)
VALUES ('What is a correct syntax to output "Hello World" in Java?', 'System.out.println("Hello World!");',
        'print("Hello World!");', 'sout("Hello World!");', 'Systemout.print("Hello world!");'),
       ('What is the correct way to create an object called foo of Bar class?', 'Bar foo = new Bar();',
        'Foo bar = new Foo();', 'Bar foo() = new Foo();', 'Foo bar() = new Bar();'),
       ('Which operator can be used to compare two values?', '==', '=', '&', '==='),
       ('Which method can be used to return a string in upper case letters?', 'toUpperCase()', 'camelCase()',
        'upperCase()', 'formatUpper()'),
       ('Which method can be used to find the length of a string?', 'length()', 'getSize()', 'len()', 'getLength()'),
       ('Which data type is used to create a variable that should store text?', 'String', 'Text', 'Varchar', 'const'),
       ('How to insert a comment?', '// like this', '# like this', '<-- like this -->', '/ like this');