(ns metabase.api.collection
  "/api/collection endpoints."
  (:require [compojure.core :refer [GET POST DELETE PUT]]
            [schema.core :as s]
            [metabase.api.common :as api]
            [metabase.db :as db]
            (metabase.models [collection :refer [Collection]]
                             [interface :as models])
            [metabase.util.schema :as su]))


(api/defendpoint GET "/"
  "Fetch a list of all (non-archived) Collections that the current user has read permissions for."
  []
  (filterv models/can-read? (db/select Collection :archived false {:order-by [[:%lower.name :asc]]})))

(api/defendpoint GET "/:id"
  "Fetch a specific (non-archived) Collection, including cards that belong to it."
  [id]
  ;; TODO - hydrate the `:cards` that belong to this Collection
  (api/read-check Collection id, :archived false))

(api/defendpoint POST "/"
  "Create a new Collection."
  [:as {{:keys [name color description]} :body}]
  {name su/NonBlankString, color #"^[0-9A-Fa-f]{6}$", description (s/maybe su/NonBlankString)}
  (api/check-superuser)
  (db/insert! Collection
    :name  name
    :color color))

(api/defendpoint PUT "/:id"
  "Modify an existing Collection, including archiving or unarchiving it."
  [id, :as {{:keys [name color description archived]} :body}]
  {name su/NonBlankString, color #"^[0-9A-Fa-f]{6}$", description (s/maybe su/NonBlankString), archived (s/maybe s/Bool)}
  ;; you have to be a superuser to modify a Collection itself, but `/collection/:id/` perms are sufficient for adding/removing Cards
  (api/check-superuser)
  (api/check-exists? Collection id)
  (db/update! Collection id
    :name        name
    :color       color
    :description description
    :archived    (if (nil? archived)
                   false
                   archived)))








(api/define-routes)