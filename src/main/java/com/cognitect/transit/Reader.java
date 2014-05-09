// Copyright (c) Cognitect, Inc.
// All rights reserved.

package com.cognitect.transit;

import com.cognitect.transit.impl.JsonParser;
import com.cognitect.transit.impl.MsgpackParser;
import com.cognitect.transit.impl.Parser;
import com.cognitect.transit.impl.ReadCache;
import com.cognitect.transit.impl.decode.*;
import com.fasterxml.jackson.core.JsonFactory;
import org.msgpack.MessagePack;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Reader {

    public static enum Format { JSON, MSGPACK }

    public static IReader instance(Format type, InputStream in, Map<String, Decoder> customDecoders) throws IOException, IllegalArgumentException {
        switch (type) {
            case JSON:    return getJsonInstance(in, customDecoders);
            case MSGPACK: return getMsgpackInstance(in, customDecoders);
            default: throw new IllegalArgumentException("Unknown Reader type: " + type.toString());
        }
    }

    public static Map<String, Decoder> defaultDecoders() {

        Map<String, Decoder> decoders = new HashMap<String, Decoder>();

        decoders.put(":", new KeywordDecoder());
        decoders.put("$", new SymbolDecoder());
        decoders.put("i", new IntegerDecoder());
        decoders.put("?", new BooleanDecoder());
        decoders.put("_", new NullDecoder());
        decoders.put("f", new BigDecimalDecoder());
        decoders.put("d", new DoubleDecoder());
        decoders.put("c", new CharacterDecoder());
        decoders.put("t", new TimeDecoder());
        decoders.put("r", new URIDecoder());
        decoders.put("u", new UUIDDecoder());
        decoders.put("b", new BinaryDecoder());
        decoders.put("\'", new IdentityDecoder());
        decoders.put("set", new SetDecoder());
        decoders.put("list", new ListDecoder());
        decoders.put("ratio", new RatioDecoder());
        decoders.put("cmap", new CmapDecoder());
        decoders.put("ints", new PrimitiveArrayDecoder(PrimitiveArrayDecoder.INTS));
        decoders.put("longs", new PrimitiveArrayDecoder(PrimitiveArrayDecoder.LONGS));
        decoders.put("floats", new PrimitiveArrayDecoder(PrimitiveArrayDecoder.FLOATS));
        decoders.put("doubles", new PrimitiveArrayDecoder(PrimitiveArrayDecoder.DOUBLES));
        decoders.put("bools", new PrimitiveArrayDecoder(PrimitiveArrayDecoder.BOOLS));
        decoders.put("shorts", new PrimitiveArrayDecoder(PrimitiveArrayDecoder.SHORTS));
        decoders.put("chars", new PrimitiveArrayDecoder(PrimitiveArrayDecoder.CHARS));

        return decoders;
    }

    static IReader getJsonInstance(InputStream in, Map<String, Decoder> customDecoders) throws IOException {

        JsonFactory jf = new JsonFactory();

        Map<String, Decoder> decoders = defaultDecoders();
        if(customDecoders != null) {
            Iterator<Map.Entry<String, Decoder>> i = customDecoders.entrySet().iterator();
            while(i.hasNext()) {
                Map.Entry<String, Decoder> e = i.next();
                decoders.put(e.getKey(), e.getValue());
            }
        }

        final Parser p = new JsonParser(jf.createParser(in), decoders);
        return new IReader() {
            public Object read() throws IOException {
                return p.parse(new ReadCache());
            }
        };
    }

    static IReader getMsgpackInstance(InputStream in, Map<String, Decoder> customDecoders) throws IOException {

        MessagePack mp = new MessagePack();

        Map<String, Decoder> decoders = defaultDecoders();
        if(customDecoders != null) {
            Iterator<Map.Entry<String, Decoder>> i = customDecoders.entrySet().iterator();
            while(i.hasNext()) {
                Map.Entry<String, Decoder> e = i.next();
                decoders.put(e.getKey(), e.getValue());
            }
        }

        final Parser p = new MsgpackParser(mp.createUnpacker(in), decoders);
        return new IReader() {
            public Object read() throws IOException {
                return p.parse(new ReadCache());
            }
        };
    }
}