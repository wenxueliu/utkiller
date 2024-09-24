package com.ut.killer.execute;

import java.util.*;

public class MethodSignatureParser {
    public static List<String> parseMethodSignature(String signature) {
        ParseInfo parseInfo;
        parseInfo = new ParseInfo("", 1); // Reset index for each call
        List<String> parameterTypes = new ArrayList<>();
        while (signature.charAt(parseInfo.getNewIndex()) != ')') {
            parseInfo = parseType(signature, parseInfo.getNewIndex());
            parameterTypes.add(parseInfo.getValue());
        }
        ParseInfo returnInfo = parseType(signature, parseInfo.getNewIndex() + 1);
        parameterTypes.add(returnInfo.getValue());
        return parameterTypes;
    }

    private static ParseInfo parseType(String signature, int index) {
        switch (signature.charAt(index)) {
            case 'L':
                return parseComplexType(signature, index);
            case '[':
                return parseArrayType(signature, index);
            // Add cases for primitives here if needed
            default:
                // Advance for primitives or unsupported types
                String value = String.valueOf(signature.charAt(index));
                return new ParseInfo(value, index + 1);
        }
    }

    private static ParseInfo parseComplexType(String signature, int index) {
        index++; // Skip 'L'
        StringBuilder type = new StringBuilder();
        while (index < signature.length() && signature.charAt(index) != ';') {
            if (signature.charAt(index) == '<') {
                ParseInfo obj = parseGenericType(signature, index);
                type.append(obj.getValue());
                index = obj.getNewIndex();
            } else {
                type.append(signature.charAt(index));
                index++;
            }
        }
        index++; // Skip ';'
        return new ParseInfo(type.toString().replace('/', '.'), index);
    }

    private static ParseInfo parseGenericType(String signature, int index) {
        StringBuilder generic = new StringBuilder();
        index++; // Skip '<'
        generic.append('<');
        while (signature.charAt(index) != '>') {
            ParseInfo obj = parseType(signature, index);
            generic.append(obj.getValue());
            index = obj.getNewIndex();
            if (signature.charAt(index) == ',') {
                index++;
            }
            if (signature.charAt(index) != '>') {
                generic.append(", ");
            }
        }
        index++; // Skip '>'
        generic.append('>');
        return new ParseInfo(generic.toString(), index);
    }

    private static ParseInfo parseArrayType(String signature, int index) {
        index++; // Skip '['
        ParseInfo componentType = parseType(signature, index);
        return new ParseInfo(componentType.getValue() + "[]", componentType.getNewIndex());
    }
}