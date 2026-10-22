-- liquibase formatted sql
-- changeset elasticdata-forms:update_db_elasticdata-forms-2.0.1-2.0.2.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- The right of the module was never created (the feature was unreachable): same rows as init_core_elasticdata-forms.sql of a fresh install.
-- Destination 2.0.2: the liquibase runner only runs an update whose destination is strictly greater than the installed version (2.0.1-beta, 2.0.1-SNAPSHOT).

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
