package com.google.cloud.pubsub.client.demos.cli;

import com.google.api.services.pubsub.Pubsub;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Main class for the Cloud Pub/Sub command line sample application.
 */
public class Main {

    static final int BATCH_SIZE = 10;

    private static final String SERVICE_ACCOUNT_EMAIL_ENV_NAME =
            "SERVICE_ACCOUNT_EMAIL";

    private static final String P12_PATH_ENV_NAME = "P12_PATH";

    static final String LOOP_ENV_NAME = "LOOP";

    private static Options options;

    static {
        options = new Options();
        options.addOption("p", "p12_path", true,
                "Path to a secret file of your Service Account");
        options.addOption("e", "service_account_email", true,
                "The e-mail address of your Service Account");
        options.addOption("l", "loop", false,
                "Loop forever for pulling when specified");

    }

    /**
     * Enum representing subcommands.
     */
    private enum CmdLineOperation {
        create_topic {
            @Override
            void run(Pubsub client, String[] args) throws IOException {
                TopicMethods.createTopic(client, args);
            }
        }, publish_message {
            @Override
            void run(Pubsub client, String[] args) throws IOException {
                TopicMethods.publishMessage(client, args);
            }
        }, connect_irc {
            @Override
            void run(Pubsub client, String[] args) throws IOException {
                TopicMethods.connectIrc(client, args);
            }
        }, list_topics {
            @Override
            void run(Pubsub client, String[] args) throws IOException {
                TopicMethods.listTopics(client, args);
            }
        }, delete_topic {
            @Override
            void run(Pubsub client, String[] args) throws IOException {
                TopicMethods.deleteTopic(client, args);
            }
        }, create_subscription {
            @Override
            void run(Pubsub client, String[] args) throws IOException {
                SubscriptionMethods.createSubscription(client, args);
            }
        }, pull_messages {
            @Override
            void run(Pubsub client, String[] args) throws IOException {
                SubscriptionMethods.pullMessages(client, args);
            }
        }, list_subscriptions {
            @Override
            void run(Pubsub client, String[] args) throws IOException {
                SubscriptionMethods.listSubscriptions(client, args);
            }
        }, delete_subscription {
            @Override
            void run(Pubsub client, String[] args) throws IOException {
                SubscriptionMethods.deleteSubscription(client, args);
            }
        };
        abstract void run(Pubsub client, String[] args) throws IOException;
    }

    static void checkArgsLength(String[] args, int min) {
        if (args.length < min) {
            help();
            System.exit(1);
        }
    }

    public static void help() {
        System.err.println("Usage: pubsub-sample.[sh|bat] [options] arguments");
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter writer = new PrintWriter(System.err);
        formatter.printOptions(writer, 80, options, 2, 2);
        writer.print("Available arguments are:\n"
                        + "PROJ list_topics\n"
                        + "PROJ create_topic TOPIC\n"
                        + "PROJ delete_topic TOPIC\n"
                        + "PROJ list_subscriptions\n"
                        + "PROJ create_subscription SUBSCRIPTION LINKED_TOPIC\n"
                        + "PROJ delete_subscription SUBSCRIPTION\n"
                        + "PROJ connect_irc TOPIC SERVER CHANNEL\n"
                        + "PROJ publish_message TOPIC MESSAGE\n"
                        + "PROJ pull_messages SUBSCRIPTION\n"
        );
        writer.close();
    }

    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);
        String[] cmdArgs = cmd.getArgs();
        checkArgsLength(cmdArgs, 2);
        String serviceAccountEmail = System.getenv(
                SERVICE_ACCOUNT_EMAIL_ENV_NAME);
        String p12Path = System.getenv(P12_PATH_ENV_NAME);
        if (cmd.hasOption("service_account_email")) {
            serviceAccountEmail = cmd.getOptionValue("service_account_email");
        }
        if (cmd.hasOption("p12_path")) {
            p12Path = cmd.getOptionValue("p12_path");
        }
        if (cmd.hasOption("loop")) {
            System.setProperty(LOOP_ENV_NAME, "loop");
        }
        Pubsub client = Utils.getClient(serviceAccountEmail, p12Path);
        try {
            CmdLineOperation cmdLineOperation =
                    CmdLineOperation.valueOf(cmdArgs[1]);
            cmdLineOperation.run(client, cmdArgs);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalArgumentException e) {
            help();
            System.exit(1);
        }
    }
}
