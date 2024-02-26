package bartbokkers.sellblocks.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            String partialCommand = args[0].toLowerCase();
            if ("give".startsWith(partialCommand)) {
                suggestions.add("give");
            }
            if ("add".startsWith(partialCommand)) {
                suggestions.add("add");
            }
            if ("remove".startsWith(partialCommand)) {
                suggestions.add("remove");
            }
            if ("collect".startsWith(partialCommand)) {
                suggestions.add("collect");
            }
            return suggestions;
        }
        return null;
    }

}
