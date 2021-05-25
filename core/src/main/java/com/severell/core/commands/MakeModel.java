package com.severell.core.commands;

import com.severell.core.database.TableMetaData;
import com.squareup.javapoet.*;
import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.utils.StringUtils;
import picocli.CommandLine;

import javax.lang.model.element.Modifier;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(name="make:model", mixinStandardHelpOptions = true, version = "0.1", description = "Create a new model" )
public class MakeModel extends MakeableCommand {

    @CommandLine.Option(names = {"-t", "--table"}, description = "Table Name To Use")
    private String table;

    @CommandLine.Parameters(index = "0", description = "Model name")
    private String name;


    @Override
    public void execute() throws IOException {
        setConnection(getConnection());
        String modelName = name;
        String tableName = table == null ? modelName.toLowerCase() : table;

        TableMetaData metaData = connection.metaData(tableName);

        TypeSpec model = getTypeSpec(tableName, metaData, modelName);

        JavaFile javaFile = JavaFile.builder(this.calleePackage + ".models", model)
                .build();

        String[] packageArray = javaFile.packageName.split("\\.");
        String packageName = StringUtils.join(packageArray,"/");
        writer = writer == null ? new FileWriter(new File("src/main/java/" + packageName + "/" +javaFile.typeSpec.name + ".java")) : writer;
        make(javaFile);

        CommandLogger.printlnGreen(String.format("Created Model - %s", name));

        System.out.println("");
        CommandLogger.printlnGreen(String.format("Compiling & Processing Model"));
        runMavenGoals("compile","process-classes");

    }

    private TypeSpec getTypeSpec(String tableName, TableMetaData metaData, String modelName) {
        ArrayList<FieldSpec> fields = new ArrayList<>();
        ArrayList<MethodSpec> methods = new ArrayList<>();

        setPrimaryKeyAnnotation(metaData, fields);

        for(ColumnMetaData col : metaData.getColumns()) {
            if(!col.isPrimaryKey()) {
                FieldSpec.Builder builder = FieldSpec.builder(col.getType().getJavaType(), col.getVariableName());
                if(col.getColumnName().equals("created_at")) {
                    builder.addAnnotation(WhenCreated.class);
                }
                if(col.getColumnName().equals("updated_at")) {
                    builder.addAnnotation(WhenModified.class);
                }

                fields.add(builder.build());
            }

            MethodSpec get = MethodSpec.methodBuilder(String.format("get%s", col.getSetterName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement(String.format("return this.%s", col.getVariableName()))
                    .returns(col.getType().getJavaType()).build();

            MethodSpec set = MethodSpec.methodBuilder(String.format("set%s", col.getSetterName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(col.getType().getJavaType(), col.getVariableName())
                    .addStatement(String.format("this.%s = %s", col.getVariableName(), col.getVariableName()))
                    .returns(void.class).build();

            methods.add(get);
            methods.add(set);
        }

//        char[] chars = tableName.toCharArray();
//        chars[0] = Character.toUpperCase(chars[0]);
//        modelName = new String(chars);

        AnnotationSpec annotationSpec = AnnotationSpec.builder(Table.class)
                .addMember("name", "$S", tableName)
                .build();

        TypeSpec model = TypeSpec.classBuilder(modelName)
                .addAnnotation(Entity.class)
                .addAnnotation(annotationSpec)
                .superclass(Model.class)
                .addModifiers(Modifier.PUBLIC)
                .addFields(fields)
                .addMethods(methods)
                .build();
        return model;
    }

    private void setPrimaryKeyAnnotation(TableMetaData metaData, ArrayList<FieldSpec> fields) {
        List<ColumnMetaData> primaryKeyList = metaData.getColumns().stream()
                .filter((ColumnMetaData col) -> col.isPrimaryKey())
                .collect(Collectors.toList());

        if(primaryKeyList.size() > 1) {
            //TODO Handle Composite Keys
        } else if(primaryKeyList.size() == 1){
            ColumnMetaData col = primaryKeyList.get(0);
            FieldSpec field = FieldSpec.builder(col.getType().getJavaType(), col.getVariableName())
                    .addAnnotation(Id.class)
                    .build();
            fields.add(field);
        }
    }

}
