// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: keys.proto

package protos;

public final class KeyProto {
  private KeyProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface KeysOrBuilder extends
      // @@protoc_insertion_point(interface_extends:protos.Keys)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>repeated int32 key = 1 [packed = true];</code>
     */
    java.util.List<java.lang.Integer> getKeyList();
    /**
     * <code>repeated int32 key = 1 [packed = true];</code>
     */
    int getKeyCount();
    /**
     * <code>repeated int32 key = 1 [packed = true];</code>
     */
    int getKey(int index);
  }
  /**
   * Protobuf type {@code protos.Keys}
   */
  public static final class Keys extends
      com.google.protobuf.GeneratedMessage implements
      // @@protoc_insertion_point(message_implements:protos.Keys)
      KeysOrBuilder {
    // Use Keys.newBuilder() to construct.
    private Keys(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private Keys(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final Keys defaultInstance;
    public static Keys getDefaultInstance() {
      return defaultInstance;
    }

    public Keys getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private Keys(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 8: {
              if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                key_ = new java.util.ArrayList<java.lang.Integer>();
                mutable_bitField0_ |= 0x00000001;
              }
              key_.add(input.readInt32());
              break;
            }
            case 10: {
              int length = input.readRawVarint32();
              int limit = input.pushLimit(length);
              if (!((mutable_bitField0_ & 0x00000001) == 0x00000001) && input.getBytesUntilLimit() > 0) {
                key_ = new java.util.ArrayList<java.lang.Integer>();
                mutable_bitField0_ |= 0x00000001;
              }
              while (input.getBytesUntilLimit() > 0) {
                key_.add(input.readInt32());
              }
              input.popLimit(limit);
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e.getMessage()).setUnfinishedMessage(this);
      } finally {
        if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
          key_ = java.util.Collections.unmodifiableList(key_);
        }
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return protos.KeyProto.internal_static_protos_Keys_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return protos.KeyProto.internal_static_protos_Keys_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              protos.KeyProto.Keys.class, protos.KeyProto.Keys.Builder.class);
    }

    public static com.google.protobuf.Parser<Keys> PARSER =
        new com.google.protobuf.AbstractParser<Keys>() {
      public Keys parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new Keys(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<Keys> getParserForType() {
      return PARSER;
    }

    public static final int KEY_FIELD_NUMBER = 1;
    private java.util.List<java.lang.Integer> key_;
    /**
     * <code>repeated int32 key = 1 [packed = true];</code>
     */
    public java.util.List<java.lang.Integer>
        getKeyList() {
      return key_;
    }
    /**
     * <code>repeated int32 key = 1 [packed = true];</code>
     */
    public int getKeyCount() {
      return key_.size();
    }
    /**
     * <code>repeated int32 key = 1 [packed = true];</code>
     */
    public int getKey(int index) {
      return key_.get(index);
    }
    private int keyMemoizedSerializedSize = -1;

    private void initFields() {
      key_ = java.util.Collections.emptyList();
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (getKeyList().size() > 0) {
        output.writeRawVarint32(10);
        output.writeRawVarint32(keyMemoizedSerializedSize);
      }
      for (int i = 0; i < key_.size(); i++) {
        output.writeInt32NoTag(key_.get(i));
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      {
        int dataSize = 0;
        for (int i = 0; i < key_.size(); i++) {
          dataSize += com.google.protobuf.CodedOutputStream
            .computeInt32SizeNoTag(key_.get(i));
        }
        size += dataSize;
        if (!getKeyList().isEmpty()) {
          size += 1;
          size += com.google.protobuf.CodedOutputStream
              .computeInt32SizeNoTag(dataSize);
        }
        keyMemoizedSerializedSize = dataSize;
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static protos.KeyProto.Keys parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static protos.KeyProto.Keys parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static protos.KeyProto.Keys parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static protos.KeyProto.Keys parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static protos.KeyProto.Keys parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static protos.KeyProto.Keys parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static protos.KeyProto.Keys parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static protos.KeyProto.Keys parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static protos.KeyProto.Keys parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static protos.KeyProto.Keys parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(protos.KeyProto.Keys prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code protos.Keys}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:protos.Keys)
        protos.KeyProto.KeysOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return protos.KeyProto.internal_static_protos_Keys_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return protos.KeyProto.internal_static_protos_Keys_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                protos.KeyProto.Keys.class, protos.KeyProto.Keys.Builder.class);
      }

      // Construct using protos.KeyProto.Keys.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        key_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return protos.KeyProto.internal_static_protos_Keys_descriptor;
      }

      public protos.KeyProto.Keys getDefaultInstanceForType() {
        return protos.KeyProto.Keys.getDefaultInstance();
      }

      public protos.KeyProto.Keys build() {
        protos.KeyProto.Keys result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public protos.KeyProto.Keys buildPartial() {
        protos.KeyProto.Keys result = new protos.KeyProto.Keys(this);
        int from_bitField0_ = bitField0_;
        if (((bitField0_ & 0x00000001) == 0x00000001)) {
          key_ = java.util.Collections.unmodifiableList(key_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.key_ = key_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof protos.KeyProto.Keys) {
          return mergeFrom((protos.KeyProto.Keys)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(protos.KeyProto.Keys other) {
        if (other == protos.KeyProto.Keys.getDefaultInstance()) return this;
        if (!other.key_.isEmpty()) {
          if (key_.isEmpty()) {
            key_ = other.key_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureKeyIsMutable();
            key_.addAll(other.key_);
          }
          onChanged();
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        protos.KeyProto.Keys parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (protos.KeyProto.Keys) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private java.util.List<java.lang.Integer> key_ = java.util.Collections.emptyList();
      private void ensureKeyIsMutable() {
        if (!((bitField0_ & 0x00000001) == 0x00000001)) {
          key_ = new java.util.ArrayList<java.lang.Integer>(key_);
          bitField0_ |= 0x00000001;
         }
      }
      /**
       * <code>repeated int32 key = 1 [packed = true];</code>
       */
      public java.util.List<java.lang.Integer>
          getKeyList() {
        return java.util.Collections.unmodifiableList(key_);
      }
      /**
       * <code>repeated int32 key = 1 [packed = true];</code>
       */
      public int getKeyCount() {
        return key_.size();
      }
      /**
       * <code>repeated int32 key = 1 [packed = true];</code>
       */
      public int getKey(int index) {
        return key_.get(index);
      }
      /**
       * <code>repeated int32 key = 1 [packed = true];</code>
       */
      public Builder setKey(
          int index, int value) {
        ensureKeyIsMutable();
        key_.set(index, value);
        onChanged();
        return this;
      }
      /**
       * <code>repeated int32 key = 1 [packed = true];</code>
       */
      public Builder addKey(int value) {
        ensureKeyIsMutable();
        key_.add(value);
        onChanged();
        return this;
      }
      /**
       * <code>repeated int32 key = 1 [packed = true];</code>
       */
      public Builder addAllKey(
          java.lang.Iterable<? extends java.lang.Integer> values) {
        ensureKeyIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, key_);
        onChanged();
        return this;
      }
      /**
       * <code>repeated int32 key = 1 [packed = true];</code>
       */
      public Builder clearKey() {
        key_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:protos.Keys)
    }

    static {
      defaultInstance = new Keys(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:protos.Keys)
  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_protos_Keys_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_protos_Keys_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\nkeys.proto\022\006protos\"\027\n\004Keys\022\017\n\003key\030\001 \003(" +
      "\005B\002\020\001B\022\n\006protosB\010KeyProto"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_protos_Keys_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_protos_Keys_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_protos_Keys_descriptor,
        new java.lang.String[] { "Key", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
