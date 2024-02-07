package net.george.blueprint.core.util.extension;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.george.blueprint.core.api.recipe.ConditionalAdvancement;
import net.george.blueprint.core.api.recipe.condition.ICondition;
import net.george.blueprint.core.events.AdvancementBuildingEvent;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("unused")
public interface AdvancementBuilderExtension {
    static Advancement.Builder fromJson(JsonObject obj, AdvancementEntityPredicateDeserializer predicateDeserializer, ICondition.IContext context) {
        if ((obj = ConditionalAdvancement.processConditional(obj, context)) == null) {
            return null;
        }
        Identifier identifier = obj.has("parent") ? new Identifier(JsonHelper.getString(obj, "parent")) : null;
        AdvancementDisplay advancementDisplay = obj.has("display") ? AdvancementDisplay.fromJson(JsonHelper.getObject(obj, "display")) : null;
        AdvancementRewards advancementRewards = obj.has("rewards") ? AdvancementRewards.fromJson(JsonHelper.getObject(obj, "rewards")) : AdvancementRewards.NONE;
        Map<String, AdvancementCriterion> map = AdvancementCriterion.criteriaFromJson(JsonHelper.getObject(obj, "criteria"), predicateDeserializer);
        if (map.isEmpty()) {
            throw new JsonSyntaxException("Advancement criteria cannot be empty");
        } else {
            JsonArray jsonArray = JsonHelper.getArray(obj, "requirements", new JsonArray());
            String[][] strings = new String[jsonArray.size()][];

            int i;
            int j;
            for(i = 0; i < jsonArray.size(); ++i) {
                JsonArray jsonArray2 = JsonHelper.asArray(jsonArray.get(i), "requirements[" + i + "]");
                strings[i] = new String[jsonArray2.size()];

                for(j = 0; j < jsonArray2.size(); ++j) {
                    strings[i][j] = JsonHelper.asString(jsonArray2.get(j), "requirements[" + i + "][" + j + "]");
                }
            }

            if (strings.length == 0) {
                strings = new String[map.size()][];
                i = 0;

                String string;
                for(Iterator<String> var16 = map.keySet().iterator(); var16.hasNext(); strings[i++] = new String[]{string}) {
                    string = var16.next();
                }
            }

            String[][] var17 = strings;
            int var18 = strings.length;

            int var13;
            for(j = 0; j < var18; ++j) {
                String[] strings2 = var17[j];
                if (strings2.length == 0 && map.isEmpty()) {
                    throw new JsonSyntaxException("Requirement entry cannot be empty");
                }

                var13 = strings2.length;

                for(int var14 = 0; var14 < var13; ++var14) {
                    String string2 = strings2[var14];
                    if (!map.containsKey(string2)) {
                        throw new JsonSyntaxException("Unknown required criterion '" + string2 + "'");
                    }
                }
            }

            Iterator<String> var19 = map.keySet().iterator();

            String string3;
            boolean bl;
            do {
                if (!var19.hasNext()) {
                    Advancement.Builder result = new Advancement.Builder(identifier, advancementDisplay, advancementRewards, map, strings);
                    AdvancementBuildingEvent.onBuildingAdvancement(result, predicateDeserializer.getAdvancementId());

                    return result;
                }

                string3 = var19.next();
                bl = false;
                int var24 = strings.length;

                for(var13 = 0; var13 < var24; ++var13) {
                    String[] strings3 = strings[var13];
                    if (ArrayUtils.contains(strings3, string3)) {
                        bl = true;
                        break;
                    }
                }
            } while(bl);

            throw new JsonSyntaxException("Criterion '" + string3 + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
        }
    }
}
