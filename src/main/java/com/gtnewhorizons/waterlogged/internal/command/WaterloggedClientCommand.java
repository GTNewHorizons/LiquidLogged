package com.gtnewhorizons.waterlogged.internal.command;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fluids.Fluid;

import com.gtnewhorizons.waterlogged.api.WaterloggedAPI;

/** Client command that shows the fluid the client world has for a block: {@code /waterlogged_client x y z}. */
public class WaterloggedClientCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "waterlogged_client";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/waterlogged_client <x> <y> <z>";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 3) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        ChunkCoordinates here = sender.getPlayerCoordinates();
        int x = MathHelper.floor_double(func_110666_a(sender, here.posX, args[0]));
        int y = MathHelper.floor_double(func_110666_a(sender, here.posY, args[1]));
        int z = MathHelper.floor_double(func_110666_a(sender, here.posZ, args[2]));
        Fluid fluid = WaterloggedAPI.getFluid(Minecraft.getMinecraft().theWorld, x, y, z);
        WaterloggedCommand.reply(sender, "client: " + (fluid == null ? "none" : fluid.getName()));
    }
}
