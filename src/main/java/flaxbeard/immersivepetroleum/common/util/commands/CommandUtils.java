package flaxbeard.immersivepetroleum.common.util.commands;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class CommandUtils{
	static void sendHelp(CommandSourceStack source, String subIdent){
		sendTranslated(source, "chat.immersivepetroleum.command.reservoir" + subIdent + ".help");
	}
	
	static void sendString(CommandSourceStack source, String str){
		source.sendSuccess(() -> Component.literal(str), true);
	}
	
	static void sendStringError(CommandSourceStack source, String str){
		source.sendSuccess(() -> Component.literal(str).withStyle(ChatFormatting.RED), true);
	}
	
	static void sendTranslated(CommandSourceStack source, String translationKey, Object... args){
		source.sendSuccess(() -> Component.translatable(translationKey, args), true);
	}
	
	static void sendTranslatedError(CommandSourceStack source, String translationKey, Object... args){
		source.sendSuccess(() -> Component.translatable(translationKey, args).withStyle(ChatFormatting.RED), true);
	}
}
