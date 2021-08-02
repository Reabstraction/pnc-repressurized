package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCoordinateOperator extends ProgWidget implements IVariableSetWidget {
    private EnumOperator operator = EnumOperator.PLUS_MINUS;
    private String variable = "";
    private DroneAIManager aiManager;
    private final AxisOptions axisOptions = new AxisOptions(true, true, true);

    public ProgWidgetCoordinateOperator() {
        super(ModProgWidgets.COORDINATE_OPERATOR.get());
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.COORDINATE.get());
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.GRAY;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (variable.equals("")) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.general.error.emptyVariable"));
        }
        if (!axisOptions.shouldCheck(Axis.X) && !axisOptions.shouldCheck(Axis.Y) && !axisOptions.shouldCheck(Axis.Z)) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.conditionCoordinate.error.noAxisSelected"));
        }
        if (operator == EnumOperator.MAX_MIN) {
            if (getConnectedParameters()[0] == null && getConnectedParameters()[getParameters().size()] == null) {
                curInfo.add(xlate("pneumaticcraft.gui.progWidget.coordinateOperator.noParameter"));
            }
        } else if (operator == EnumOperator.MULIPLY_DIVIDE) {
            IProgWidget w = getConnectedParameters()[1];
            while (w instanceof ProgWidgetCoordinate) {
                if (!((ProgWidgetCoordinate) w).isUsingVariable()) {
                    BlockPos pos = ((ProgWidgetCoordinate) w).getCoordinate();
                    if (axisOptions.shouldCheck(Axis.X) && pos.getX() == 0
                            || axisOptions.shouldCheck(Axis.Y) && pos.getY() == 0
                            || axisOptions.shouldCheck(Axis.Z) && pos.getZ() == 0)
                    {
                        curInfo.add(xlate("pneumaticcraft.gui.progWidget.coordinateOperator.divideByZero"));
                        break;
                    }
                }
                w = w.getConnectedParameters()[0];
            }
        }
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets) {
        if (!variable.equals("")) {
            BlockPos curPos = calculateCoordinate(this, 0, operator, axisOptions);
            aiManager.setCoordinate(variable, curPos);
        }
        return super.getOutputWidget(drone, allWidgets);
    }

    public static BlockPos calculateCoordinate(IProgWidget widget, int argIndex, EnumOperator operator, AxisOptions axisOptions) {
        return doCalc(
                (ProgWidgetCoordinate) widget.getConnectedParameters()[argIndex],
                (ProgWidgetCoordinate) widget.getConnectedParameters()[argIndex + widget.getParameters().size()],
                operator, axisOptions
        );
    }

    public static BlockPos calculateCoordinate(IProgWidget widget, int argIndex, EnumOperator operator) {
        return calculateCoordinate(widget, argIndex, operator, AxisOptions.TRUE);
    }

    private static BlockPos doCalc(ProgWidgetCoordinate whiteList, ProgWidgetCoordinate blackList, EnumOperator op, AxisOptions axisOptions) {
        BlockPos curPos = op.initialValue(whiteList, blackList);
        if (whiteList != null) {
            whiteList = (ProgWidgetCoordinate) whiteList.getConnectedParameters()[0];
            while (whiteList != null) {
                curPos = getNextPos(curPos, whiteList.getCoordinate(), op, true, axisOptions);
                whiteList = (ProgWidgetCoordinate) whiteList.getConnectedParameters()[0];
            }
        } else if (blackList != null) {
            // we already picked up the first blacklist coord via op.initialValue()
            blackList = (ProgWidgetCoordinate) blackList.getConnectedParameters()[0];
        }
        while (blackList != null) {
            curPos = getNextPos(curPos, blackList.getCoordinate(), op, false, axisOptions);
            blackList = (ProgWidgetCoordinate) blackList.getConnectedParameters()[0];
        }
        return curPos;
    }

    private static BlockPos getNextPos(BlockPos curPos, BlockPos rawPos, EnumOperator op, boolean isWhiteList, AxisOptions axisOptions) {
        BlockPos pos = new BlockPos(
                axisOptions.shouldCheck(Axis.X) ? rawPos.getX() : op.defaultValue(isWhiteList),
                axisOptions.shouldCheck(Axis.Y) ? rawPos.getY() : op.defaultValue(isWhiteList),
                axisOptions.shouldCheck(Axis.Z) ? rawPos.getZ() : op.defaultValue(isWhiteList)
        );
        return op.apply(curPos, pos, isWhiteList);
    }

    @Override
    public ResourceLocation getTexture() {
        return operator.texture;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        if (!variable.isEmpty()) tag.putString("variable", variable);
        tag.putByte("operator", (byte) operator.ordinal());
        axisOptions.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        variable = tag.getString("variable");
        operator = EnumOperator.values()[tag.getByte("operator")];
        axisOptions.readFromNBT(tag, true);
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeString(variable);
        buf.writeByte(operator.ordinal());
        axisOptions.writeToBuffer(buf);
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        variable = buf.readString(GlobalVariableManager.MAX_VARIABLE_LEN);
        operator = EnumOperator.values()[buf.readByte()];
        axisOptions.readFromBuffer(buf);
    }

    public EnumOperator getOperator() {
        return operator;
    }

    public void setOperator(EnumOperator operator) {
        this.operator = operator;
    }

    @Override
    public String getVariable() {
        return variable;
    }

    @Override
    public void setVariable(String variable) {
        this.variable = variable;
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
    }

    public AxisOptions getAxisOptions() {
        return axisOptions;
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);

        curTooltip.add(xlate("pneumaticcraft.gui.progWidget.itemAssign.settingVariable", variable));
        curTooltip.add(xlate("pneumaticcraft.gui.progWidget.coordinateOperator.operator").appendString(" ").append(xlate(operator.getTranslationKey())));
        getAxesString().ifPresent(t -> curTooltip.add(xlate("pneumaticcraft.gui.progWidget.coordinateOperator.axes").appendString(" ").append(t)));
    }

    @Override
    public List<ITextComponent> getExtraStringInfo() {
        ImmutableList.Builder<ITextComponent> builder = ImmutableList.builder();
        builder.add(varAsTextComponent(variable), xlate(operator.getTranslationKey()));
        getAxesString().ifPresent(builder::add);
        return builder.build();
    }

    private Optional<ITextComponent> getAxesString() {
        List<String> l = Arrays.stream(Axis.values())
                .filter(axisOptions::shouldCheck)
                .map(axis -> axis.getName2().toUpperCase())
                .collect(Collectors.toList());
        return !l.isEmpty() && l.size() < 3 ? Optional.of(new StringTextComponent(String.join("/", l))) : Optional.empty();
    }

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(variable);
    }

    public enum EnumOperator implements ITranslatableEnum {
        PLUS_MINUS("plus_minus", 0, 0),
        MULIPLY_DIVIDE("multiply_divide", 1, 1),
        MAX_MIN("max_min", Integer.MIN_VALUE, Integer.MAX_VALUE);

        public final ResourceLocation texture;
        private final String name;
        private final int defWhite;
        private final int defBlack;

        EnumOperator(String name, int defWhite, int defBlack) {
            this.name = name;
            this.texture = Textures.progWidgetTexture("coordinate_operation_" + name + ".png");
            this.defWhite = defWhite;
            this.defBlack = defBlack;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.progWidget.coordinateOperator." + name;
        }

        public int defaultValue(boolean isWhiteList) {
            // a default to return if the relevant x/y/z field is not being operated on
            return isWhiteList ? defWhite : defBlack;
        }

        public BlockPos initialValue(ProgWidgetCoordinate whiteList, ProgWidgetCoordinate blackList) {
            switch (this) {
                case PLUS_MINUS:
                    return whiteList != null ?
                            whiteList.getCoordinate() :
                            (blackList != null ? BlockPos.ZERO.subtract(blackList.getCoordinate()) : BlockPos.ZERO);
                case MULIPLY_DIVIDE:
                    return whiteList != null ? whiteList.getCoordinate() : BlockPos.ZERO;
                case MAX_MIN:
                    return whiteList != null ?
                            whiteList.getCoordinate() :
                            (blackList != null ? blackList.getCoordinate() : BlockPos.ZERO);
                default:
                    throw new IllegalArgumentException("bad value for op?");
            }
        }

        public BlockPos apply(BlockPos p1, BlockPos p2, boolean isWhiteList) {
            switch (this) {
                case PLUS_MINUS:
                    return isWhiteList ?
                            new BlockPos(p1.getX() + p2.getX(), p1.getY() + p2.getY(), p1.getZ() + p2.getZ()) :
                            new BlockPos(p1.getX() - p2.getX(), p1.getY() - p2.getY(), p1.getZ() - p2.getZ());
                case MULIPLY_DIVIDE:
                    if (!isWhiteList && (p2.getX() == 0 || p2.getY() == 0 || p2.getZ() == 0)) return p1;  // no divide by zero!
                    return isWhiteList ?
                            new BlockPos(p1.getX() * p2.getX(), p1.getY() * p2.getY(), p1.getZ() * p2.getZ()) :
                            new BlockPos(p1.getX() / p2.getX(), p1.getY() / p2.getY(), p1.getZ() / p2.getZ());
                case MAX_MIN:
                    return isWhiteList ?
                            new BlockPos(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()), Math.max(p1.getZ(), p2.getZ())) :
                            new BlockPos(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()), Math.min(p1.getZ(), p2.getZ()));
                default:
                    return p1;
            }
        }
    }
}
