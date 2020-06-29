package stunts.wojbot.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MessageUtils {

    public static List<String> getArguments(String message) {
        List<String> args = new ArrayList<>();
        boolean building = false;
        StringBuilder holder = new StringBuilder();
        for (String arg : message.split("\\s+")) {
            boolean start = arg.startsWith("\"");
            if (start) {
                building = true;
                arg = arg.substring(1);
            }

            if (building) {
                if (!start)
                    holder.append(" ");
                holder.append(arg);
            }

            if (building && arg.endsWith("\"")) {
                building = false;
                arg = holder.substring(0, holder.length()-1);
                holder = new StringBuilder();
            }

            if (!building) {
                args.add(arg);
            }
        }
        return args;
    }

    public static String getMsgFromArgNum(String[] arguments, int argNum) {
        List<String> argsList = Arrays.asList(arguments);
        return argsList.stream().skip(argNum).collect(Collectors.joining(" ")).toLowerCase();
    }

}
