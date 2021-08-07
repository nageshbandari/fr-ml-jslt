package com.weekend.frmljslt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/events")
@Slf4j
public class TransformController {


    @PostMapping(path = "/transforms"
            , produces = MediaType.APPLICATION_JSON_VALUE
            , consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> transformInput(@RequestBody JsonNode input) {
        log.info("input input : {}", input);

        FeatureConfig featureConfig = FeatureConfig.Initialize();
        ObjectNode objNode = (ObjectNode) input;

        //assuming all transformations apply sequentially
        featureConfig.getTransforms().forEach(t -> applyTransformation(objNode, t));
      return ResponseEntity.ok(objNode.toPrettyString());
    }

    /**
     * This will apply transformation on the input json
     * I have taken the assumption that all transformations will be applied in sequence modifying the same input
     * to result in a single output.
     *
     * IMP: Given sample represents a replacement of a node with new key and
     * child as text node, made some assumptions if the result is an object vs text node.
     *
     * @param objNode
     * @param transform
     * @return
     */
    private void applyTransformation(ObjectNode objNode, FeatureConfig.Transform transform) {
         Optional.ofNullable(getExpression(transform.getJsltExpression()))
                .ifPresent(jslt -> {
                    JsonNode resultNode = jslt.apply(objNode);
                    if(resultNode != null && !resultNode.isEmpty()) {
                        findParentAndReplace(objNode, transform, (ExpressionImpl) jslt, resultNode);
                    }
                });
    }

    private void findParentAndReplace(ObjectNode objNode, FeatureConfig.Transform t, ExpressionImpl jslt, JsonNode resultNode) {
        //Below dotExpression to find the parent of the transformation for replacing
        ExpressionNode dotExpression = dotExpression(jslt);
        if (dotExpression == null) {
            //no dot expression to process so return
            return;
        }

        //Find Parent and parentKey to replace
        String parentJslt = parentJslt(dotExpression);
        String parentKey = parentJslt.substring(parentJslt.lastIndexOf('.') + 1);
        ObjectNode parentNode = findParentNode(objNode, parentJslt);

        //replace node
        replaceNode(parentNode, parentKey, t.getName(), resultNode);

        return;
    }

    private ObjectNode findParentNode(ObjectNode objNode, String parentExpression) {

        String replaceExpression = parentExpression.substring(0, parentExpression.lastIndexOf('.'));
        return Optional.ofNullable((ObjectNode) objNode.get(replaceExpression))
                .orElse(objNode);
    }

    private String parentJslt(ExpressionNode dotExpression) {
        return dotExpression.getChildren().stream().findFirst().orElse(dotExpression).toString();
    }

    private Expression getExpression(String jsltExpression) {
        try{
            return Parser.compileString(jsltExpression);
        } catch (JsltException e) {
            //since this is a case of ML, I believe it should try to apply as many possible transformations as possible
            // and ignore errors
            log.error("Invalid transform expression, ignoring to look at other tranforms");
        }
        return null;
    }

    private void replaceNode(ObjectNode rootNode, String oldKey, String newKey, JsonNode newNode) {

        rootNode.set(newKey, newNode.size() == 1 ? newNode.fields().next().getValue() : newNode);
        rootNode.remove(oldKey);
    }

    private ExpressionNode dotExpression(ExpressionImpl jslt) {
        List<ExpressionNode> dotExpressions = jslt.getChildren()
                .stream()
                .flatMap(this::findDotExpressions)
                .filter(c -> c instanceof DotExpression)
                .collect(Collectors.toList());
        return dotExpressions.get(0);
    }

    private Stream<ExpressionNode> findDotExpressions(ExpressionNode jslt) {

        return Stream.concat(Stream.of(jslt)
                , jslt.getChildren().stream()
                    .flatMap(this::findDotExpressions));
    }

    @GetMapping(value = "/featureConfig", produces = MediaType.APPLICATION_JSON_VALUE)
    public FeatureConfig featureConfig() {
        return FeatureConfig.Initialize();
    }

    private ObjectNode applyTrans(ObjectNode objNode, FeatureConfig.Transform t) {
        //ObjectNode newCopy = objNode.deepCopy();
        Expression jslt = getExpression(t.getJsltExpression());
        if(jslt == null) {
            return objNode;
        }

        JsonNode resultNode = jslt.apply(objNode);
        if(resultNode == null || resultNode.isEmpty()) {
            return objNode;
        }

        //Below dotExpression to find the parent of the transformation for replacing
        ExpressionNode dotExpression = dotExpression((ExpressionImpl) jslt);
        if(dotExpression == null) {
            //no dot expression to process so return input
            return objNode;
        }

        //Find Parent and parentKey to replace
        String parentJslt = parentJslt(dotExpression);
        String parentKey = parentJslt.substring(parentJslt.lastIndexOf('.') + 1);
        ObjectNode parentNode = findParentNode(objNode, parentJslt);

        //replace node
        replaceNode(parentNode, parentKey, t.getName(), resultNode);

        //return replaced json
        return objNode;
    }

}
