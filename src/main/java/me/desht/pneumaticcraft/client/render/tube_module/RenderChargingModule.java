package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.tubemodules.ChargingModule;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderChargingModule extends TubeModuleRendererBase<ChargingModule> {
    private final ModelPart tip;
    private final ModelPart body;
    private final ModelPart tubeConnector;
    private final ModelPart tipBottom;
    private final ModelPart tipTop;
    private final ModelPart tipRight;
    private final ModelPart tipLeft;

    private static final String TIP = "tip";
    private static final String BODY = "body";
    private static final String TUBECONNECTOR = "tubeConnector";
    private static final String TIPBOTTOM = "tipBottom";
    private static final String TIPTOP = "tipTop";
    private static final String TIPRIGHT = "tipRight";
    private static final String TIPLEFT = "tipLeft";

    public RenderChargingModule(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(PNCModelLayers.CHARGING_MODULE);
        tip = root.getChild(TIP);
        body = root.getChild(BODY);
        tubeConnector = root.getChild(TUBECONNECTOR);
        tipBottom = root.getChild(TIPBOTTOM);
        tipTop = root.getChild(TIPTOP);
        tipRight = root.getChild(TIPRIGHT);
        tipLeft = root.getChild(TIPLEFT);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(TIP, CubeListBuilder.create().texOffs(0, 11)
                        .addBox("tip_0", 0.0F, 0.0F, 1.5F, 2, 2, 1),
                PartPose.offsetAndRotation(1.0F, 15.0F, 8.0F, 0.0F, 3.1416F, 0.0F));
        partdefinition.addOrReplaceChild(BODY, CubeListBuilder.create().texOffs(0, 6)
                        .addBox("body_0", 0.0F, 0.0F, 0.0F, 3, 3, 2),
                PartPose.offsetAndRotation(1.5F, 14.5F, 6.0F, 0.0F, 3.1416F, 0.0F));
        partdefinition.addOrReplaceChild(TUBECONNECTOR, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("tubeConnector_0", 0.0F, 0.0F, 0.0F, 4, 4, 2),
                PartPose.offsetAndRotation(2.0F, 14.0F, 4.0F, 0.0F, 3.1416F, 0.0F));
        partdefinition.addOrReplaceChild(TIPBOTTOM, CubeListBuilder.create().texOffs(0, 18)
                        .addBox("tipBottom_0", 0.0F, 2.0F, -0.5F, 2, 0, 1),
                PartPose.offset(-1.0F, 15.0F, 7.0F));
        partdefinition.addOrReplaceChild(TIPTOP, CubeListBuilder.create().texOffs(0, 14)
                        .addBox("tipTop_0", 0.0F, 0.0F, -0.5F, 2, 0, 1),
                PartPose.offset(-1.0F, 15.0F, 7.0F));
        partdefinition.addOrReplaceChild(TIPRIGHT, CubeListBuilder.create().texOffs(2, 15)
                        .addBox("tipRight_0", 0.0F, -1.0F, -0.5F, 0, 2, 1),
                PartPose.offset(-1.0F, 16.0F, 7.0F));
        partdefinition.addOrReplaceChild(TIPLEFT, CubeListBuilder.create().texOffs(0, 15)
                        .addBox("tipLeft_0", 0.0F, -1.0F, -0.5F, 0, 2, 1),
                PartPose.offset(1.0F, 16.0F, 7.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }


    @Override
    protected void renderDynamic(ChargingModule module, PoseStack matrixStack, VertexConsumer builder, float partialTicks, int combinedLight, int combinedOverlay, float alpha) {
        tip.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        body.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        tubeConnector.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        tipBottom.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        tipTop.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        tipRight.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        tipLeft.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
    }

    @Override
    protected ResourceLocation getTexture() {
        ResourceLocation texture;
        if (isUpgraded()) {
            texture = Textures.MODEL_CHARGING_MODULE_UPGRADED;
        } else {
            texture = Textures.MODEL_CHARGING_MODULE;
        }
        return texture;
    }
}
