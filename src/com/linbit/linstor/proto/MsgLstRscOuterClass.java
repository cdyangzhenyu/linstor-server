// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto/MsgLstRsc.proto

package com.linbit.linstor.proto;

public final class MsgLstRscOuterClass {
  private MsgLstRscOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface MsgLstRscOrBuilder extends
      // @@protoc_insertion_point(interface_extends:com.linbit.linstor.proto.MsgLstRsc)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <pre>
     * Resources
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
     */
    java.util.List<com.linbit.linstor.proto.RscOuterClass.Rsc> 
        getResourcesList();
    /**
     * <pre>
     * Resources
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
     */
    com.linbit.linstor.proto.RscOuterClass.Rsc getResources(int index);
    /**
     * <pre>
     * Resources
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
     */
    int getResourcesCount();
    /**
     * <pre>
     * Resources
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
     */
    java.util.List<? extends com.linbit.linstor.proto.RscOuterClass.RscOrBuilder> 
        getResourcesOrBuilderList();
    /**
     * <pre>
     * Resources
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
     */
    com.linbit.linstor.proto.RscOuterClass.RscOrBuilder getResourcesOrBuilder(
        int index);
  }
  /**
   * <pre>
   * linstor - List resources
   * </pre>
   *
   * Protobuf type {@code com.linbit.linstor.proto.MsgLstRsc}
   */
  public  static final class MsgLstRsc extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:com.linbit.linstor.proto.MsgLstRsc)
      MsgLstRscOrBuilder {
    // Use MsgLstRsc.newBuilder() to construct.
    private MsgLstRsc(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private MsgLstRsc() {
      resources_ = java.util.Collections.emptyList();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private MsgLstRsc(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
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
            case 10: {
              if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                resources_ = new java.util.ArrayList<com.linbit.linstor.proto.RscOuterClass.Rsc>();
                mutable_bitField0_ |= 0x00000001;
              }
              resources_.add(
                  input.readMessage(com.linbit.linstor.proto.RscOuterClass.Rsc.PARSER, extensionRegistry));
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
          resources_ = java.util.Collections.unmodifiableList(resources_);
        }
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.linbit.linstor.proto.MsgLstRscOuterClass.internal_static_com_linbit_linstor_proto_MsgLstRsc_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.linbit.linstor.proto.MsgLstRscOuterClass.internal_static_com_linbit_linstor_proto_MsgLstRsc_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc.class, com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc.Builder.class);
    }

    public static final int RESOURCES_FIELD_NUMBER = 1;
    private java.util.List<com.linbit.linstor.proto.RscOuterClass.Rsc> resources_;
    /**
     * <pre>
     * Resources
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
     */
    public java.util.List<com.linbit.linstor.proto.RscOuterClass.Rsc> getResourcesList() {
      return resources_;
    }
    /**
     * <pre>
     * Resources
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
     */
    public java.util.List<? extends com.linbit.linstor.proto.RscOuterClass.RscOrBuilder> 
        getResourcesOrBuilderList() {
      return resources_;
    }
    /**
     * <pre>
     * Resources
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
     */
    public int getResourcesCount() {
      return resources_.size();
    }
    /**
     * <pre>
     * Resources
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
     */
    public com.linbit.linstor.proto.RscOuterClass.Rsc getResources(int index) {
      return resources_.get(index);
    }
    /**
     * <pre>
     * Resources
     * </pre>
     *
     * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
     */
    public com.linbit.linstor.proto.RscOuterClass.RscOrBuilder getResourcesOrBuilder(
        int index) {
      return resources_.get(index);
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      for (int i = 0; i < getResourcesCount(); i++) {
        if (!getResources(i).isInitialized()) {
          memoizedIsInitialized = 0;
          return false;
        }
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      for (int i = 0; i < resources_.size(); i++) {
        output.writeMessage(1, resources_.get(i));
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      for (int i = 0; i < resources_.size(); i++) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, resources_.get(i));
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc)) {
        return super.equals(obj);
      }
      com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc other = (com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc) obj;

      boolean result = true;
      result = result && getResourcesList()
          .equals(other.getResourcesList());
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (getResourcesCount() > 0) {
        hash = (37 * hash) + RESOURCES_FIELD_NUMBER;
        hash = (53 * hash) + getResourcesList().hashCode();
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * <pre>
     * linstor - List resources
     * </pre>
     *
     * Protobuf type {@code com.linbit.linstor.proto.MsgLstRsc}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:com.linbit.linstor.proto.MsgLstRsc)
        com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRscOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.linbit.linstor.proto.MsgLstRscOuterClass.internal_static_com_linbit_linstor_proto_MsgLstRsc_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.linbit.linstor.proto.MsgLstRscOuterClass.internal_static_com_linbit_linstor_proto_MsgLstRsc_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc.class, com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc.Builder.class);
      }

      // Construct using com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
          getResourcesFieldBuilder();
        }
      }
      public Builder clear() {
        super.clear();
        if (resourcesBuilder_ == null) {
          resources_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          resourcesBuilder_.clear();
        }
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.linbit.linstor.proto.MsgLstRscOuterClass.internal_static_com_linbit_linstor_proto_MsgLstRsc_descriptor;
      }

      public com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc getDefaultInstanceForType() {
        return com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc.getDefaultInstance();
      }

      public com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc build() {
        com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc buildPartial() {
        com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc result = new com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc(this);
        int from_bitField0_ = bitField0_;
        if (resourcesBuilder_ == null) {
          if (((bitField0_ & 0x00000001) == 0x00000001)) {
            resources_ = java.util.Collections.unmodifiableList(resources_);
            bitField0_ = (bitField0_ & ~0x00000001);
          }
          result.resources_ = resources_;
        } else {
          result.resources_ = resourcesBuilder_.build();
        }
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc) {
          return mergeFrom((com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc other) {
        if (other == com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc.getDefaultInstance()) return this;
        if (resourcesBuilder_ == null) {
          if (!other.resources_.isEmpty()) {
            if (resources_.isEmpty()) {
              resources_ = other.resources_;
              bitField0_ = (bitField0_ & ~0x00000001);
            } else {
              ensureResourcesIsMutable();
              resources_.addAll(other.resources_);
            }
            onChanged();
          }
        } else {
          if (!other.resources_.isEmpty()) {
            if (resourcesBuilder_.isEmpty()) {
              resourcesBuilder_.dispose();
              resourcesBuilder_ = null;
              resources_ = other.resources_;
              bitField0_ = (bitField0_ & ~0x00000001);
              resourcesBuilder_ = 
                com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                   getResourcesFieldBuilder() : null;
            } else {
              resourcesBuilder_.addAllMessages(other.resources_);
            }
          }
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        for (int i = 0; i < getResourcesCount(); i++) {
          if (!getResources(i).isInitialized()) {
            return false;
          }
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private java.util.List<com.linbit.linstor.proto.RscOuterClass.Rsc> resources_ =
        java.util.Collections.emptyList();
      private void ensureResourcesIsMutable() {
        if (!((bitField0_ & 0x00000001) == 0x00000001)) {
          resources_ = new java.util.ArrayList<com.linbit.linstor.proto.RscOuterClass.Rsc>(resources_);
          bitField0_ |= 0x00000001;
         }
      }

      private com.google.protobuf.RepeatedFieldBuilderV3<
          com.linbit.linstor.proto.RscOuterClass.Rsc, com.linbit.linstor.proto.RscOuterClass.Rsc.Builder, com.linbit.linstor.proto.RscOuterClass.RscOrBuilder> resourcesBuilder_;

      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public java.util.List<com.linbit.linstor.proto.RscOuterClass.Rsc> getResourcesList() {
        if (resourcesBuilder_ == null) {
          return java.util.Collections.unmodifiableList(resources_);
        } else {
          return resourcesBuilder_.getMessageList();
        }
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public int getResourcesCount() {
        if (resourcesBuilder_ == null) {
          return resources_.size();
        } else {
          return resourcesBuilder_.getCount();
        }
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public com.linbit.linstor.proto.RscOuterClass.Rsc getResources(int index) {
        if (resourcesBuilder_ == null) {
          return resources_.get(index);
        } else {
          return resourcesBuilder_.getMessage(index);
        }
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public Builder setResources(
          int index, com.linbit.linstor.proto.RscOuterClass.Rsc value) {
        if (resourcesBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureResourcesIsMutable();
          resources_.set(index, value);
          onChanged();
        } else {
          resourcesBuilder_.setMessage(index, value);
        }
        return this;
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public Builder setResources(
          int index, com.linbit.linstor.proto.RscOuterClass.Rsc.Builder builderForValue) {
        if (resourcesBuilder_ == null) {
          ensureResourcesIsMutable();
          resources_.set(index, builderForValue.build());
          onChanged();
        } else {
          resourcesBuilder_.setMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public Builder addResources(com.linbit.linstor.proto.RscOuterClass.Rsc value) {
        if (resourcesBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureResourcesIsMutable();
          resources_.add(value);
          onChanged();
        } else {
          resourcesBuilder_.addMessage(value);
        }
        return this;
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public Builder addResources(
          int index, com.linbit.linstor.proto.RscOuterClass.Rsc value) {
        if (resourcesBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          ensureResourcesIsMutable();
          resources_.add(index, value);
          onChanged();
        } else {
          resourcesBuilder_.addMessage(index, value);
        }
        return this;
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public Builder addResources(
          com.linbit.linstor.proto.RscOuterClass.Rsc.Builder builderForValue) {
        if (resourcesBuilder_ == null) {
          ensureResourcesIsMutable();
          resources_.add(builderForValue.build());
          onChanged();
        } else {
          resourcesBuilder_.addMessage(builderForValue.build());
        }
        return this;
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public Builder addResources(
          int index, com.linbit.linstor.proto.RscOuterClass.Rsc.Builder builderForValue) {
        if (resourcesBuilder_ == null) {
          ensureResourcesIsMutable();
          resources_.add(index, builderForValue.build());
          onChanged();
        } else {
          resourcesBuilder_.addMessage(index, builderForValue.build());
        }
        return this;
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public Builder addAllResources(
          java.lang.Iterable<? extends com.linbit.linstor.proto.RscOuterClass.Rsc> values) {
        if (resourcesBuilder_ == null) {
          ensureResourcesIsMutable();
          com.google.protobuf.AbstractMessageLite.Builder.addAll(
              values, resources_);
          onChanged();
        } else {
          resourcesBuilder_.addAllMessages(values);
        }
        return this;
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public Builder clearResources() {
        if (resourcesBuilder_ == null) {
          resources_ = java.util.Collections.emptyList();
          bitField0_ = (bitField0_ & ~0x00000001);
          onChanged();
        } else {
          resourcesBuilder_.clear();
        }
        return this;
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public Builder removeResources(int index) {
        if (resourcesBuilder_ == null) {
          ensureResourcesIsMutable();
          resources_.remove(index);
          onChanged();
        } else {
          resourcesBuilder_.remove(index);
        }
        return this;
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public com.linbit.linstor.proto.RscOuterClass.Rsc.Builder getResourcesBuilder(
          int index) {
        return getResourcesFieldBuilder().getBuilder(index);
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public com.linbit.linstor.proto.RscOuterClass.RscOrBuilder getResourcesOrBuilder(
          int index) {
        if (resourcesBuilder_ == null) {
          return resources_.get(index);  } else {
          return resourcesBuilder_.getMessageOrBuilder(index);
        }
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public java.util.List<? extends com.linbit.linstor.proto.RscOuterClass.RscOrBuilder> 
           getResourcesOrBuilderList() {
        if (resourcesBuilder_ != null) {
          return resourcesBuilder_.getMessageOrBuilderList();
        } else {
          return java.util.Collections.unmodifiableList(resources_);
        }
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public com.linbit.linstor.proto.RscOuterClass.Rsc.Builder addResourcesBuilder() {
        return getResourcesFieldBuilder().addBuilder(
            com.linbit.linstor.proto.RscOuterClass.Rsc.getDefaultInstance());
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public com.linbit.linstor.proto.RscOuterClass.Rsc.Builder addResourcesBuilder(
          int index) {
        return getResourcesFieldBuilder().addBuilder(
            index, com.linbit.linstor.proto.RscOuterClass.Rsc.getDefaultInstance());
      }
      /**
       * <pre>
       * Resources
       * </pre>
       *
       * <code>repeated .com.linbit.linstor.proto.Rsc resources = 1;</code>
       */
      public java.util.List<com.linbit.linstor.proto.RscOuterClass.Rsc.Builder> 
           getResourcesBuilderList() {
        return getResourcesFieldBuilder().getBuilderList();
      }
      private com.google.protobuf.RepeatedFieldBuilderV3<
          com.linbit.linstor.proto.RscOuterClass.Rsc, com.linbit.linstor.proto.RscOuterClass.Rsc.Builder, com.linbit.linstor.proto.RscOuterClass.RscOrBuilder> 
          getResourcesFieldBuilder() {
        if (resourcesBuilder_ == null) {
          resourcesBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
              com.linbit.linstor.proto.RscOuterClass.Rsc, com.linbit.linstor.proto.RscOuterClass.Rsc.Builder, com.linbit.linstor.proto.RscOuterClass.RscOrBuilder>(
                  resources_,
                  ((bitField0_ & 0x00000001) == 0x00000001),
                  getParentForChildren(),
                  isClean());
          resources_ = null;
        }
        return resourcesBuilder_;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:com.linbit.linstor.proto.MsgLstRsc)
    }

    // @@protoc_insertion_point(class_scope:com.linbit.linstor.proto.MsgLstRsc)
    private static final com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc();
    }

    public static com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    @java.lang.Deprecated public static final com.google.protobuf.Parser<MsgLstRsc>
        PARSER = new com.google.protobuf.AbstractParser<MsgLstRsc>() {
      public MsgLstRsc parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new MsgLstRsc(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<MsgLstRsc> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<MsgLstRsc> getParserForType() {
      return PARSER;
    }

    public com.linbit.linstor.proto.MsgLstRscOuterClass.MsgLstRsc getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_com_linbit_linstor_proto_MsgLstRsc_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_com_linbit_linstor_proto_MsgLstRsc_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\025proto/MsgLstRsc.proto\022\030com.linbit.lins" +
      "tor.proto\032\017proto/Rsc.proto\"=\n\tMsgLstRsc\022" +
      "0\n\tresources\030\001 \003(\0132\035.com.linbit.linstor." +
      "proto.RscP\000"
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
          com.linbit.linstor.proto.RscOuterClass.getDescriptor(),
        }, assigner);
    internal_static_com_linbit_linstor_proto_MsgLstRsc_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_com_linbit_linstor_proto_MsgLstRsc_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_com_linbit_linstor_proto_MsgLstRsc_descriptor,
        new java.lang.String[] { "Resources", });
    com.linbit.linstor.proto.RscOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}