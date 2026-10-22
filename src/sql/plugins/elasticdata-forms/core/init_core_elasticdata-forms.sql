-- liquibase formatted sql
-- changeset elasticdata-forms:init_core_elasticdata-forms.sql
-- preconditions onFail:MARK_RAN onError:WARN

--
-- Data for table core_admin_right : the feature of the module (IndexElasticDataJspBean, right ELASTICDATA_FORMS_MANAGEMENT)
--
DELETE FROM core_admin_right WHERE id_right = 'ELASTICDATA_FORMS_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url,id_order) VALUES
('ELASTICDATA_FORMS_MANAGEMENT','module.elasticdata.forms.adminFeature.ManageElasticData.name',0,'jsp/admin/plugins/elasticdata/modules/forms/IndexingAppElasticData.jsp','module.elasticdata.forms.adminFeature.ManageElasticData.description',0,'elasticdata-forms',NULL,NULL,NULL,5);

--
-- Data for table core_user_right : the admin user of the core (id 1) gets the feature
--
DELETE FROM core_user_right WHERE id_right = 'ELASTICDATA_FORMS_MANAGEMENT' AND id_user = 1;
INSERT INTO core_user_right (id_right,id_user) VALUES ('ELASTICDATA_FORMS_MANAGEMENT',1);
