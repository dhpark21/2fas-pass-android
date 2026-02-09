/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.import.spec

import android.content.Context
import android.net.Uri
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.ktx.readTextFile
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.CsvParser
import com.twofasapp.feature.externalimport.import.ImportContent

internal class KeepassXcImportSpec(
    vaultsRepository: VaultsRepository,
    private val context: Context,
) : AbstractKeepassImportSpec(vaultsRepository, context) {

    companion object {
        private const val CSV_ROW_TITLE = "Title"
        private const val CSV_ROW_USERNAME = "Username"
        private const val CSV_ROW_PASSWORD = "Password"
        private const val CSV_ROW_URL = "Url"
        private const val CSV_ROW_NOTES = "Notes"
        private const val CSV_ROW_LAST_MODIFIED = "Last Modified"
        private const val CSV_ROW_CREATED = "Created"
    }

    override val type = ImportType.KeePassXC
    override val name = ImportType.KeePassXC.displayName
    override val image = com.twofasapp.core.design.R.drawable.external_logo_keepassxc
    override val instructions = context.getString(R.string.transfer_instructions_keepassxc)
    override val additionalInfo = null
    override val cta: List<Cta> = listOf(
        Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_keepassxc),
            action = CtaAction.ChooseFile,
        ),
    )

    override val xmlTagSeparator = ","

    override fun parseCsv(uri: Uri, vaultId: String): ImportContent {
        val items = buildList {
            CsvParser.parse(
                text = context.readTextFile(uri),
            ) { row ->
                add(
                    Item.create(
                        vaultId = vaultId,
                        contentType = ItemContentType.Login,
                        content = ItemContent.Login.create(
                            name = row.get(CSV_ROW_TITLE),
                            username = row.get(CSV_ROW_USERNAME),
                            password = row.get(CSV_ROW_PASSWORD),
                            url = row.get(CSV_ROW_URL),
                            notes = row.get(CSV_ROW_NOTES),
                        ),
                        createdAt = parseIsoDate(row.get(CSV_ROW_CREATED) ?: ""),
                        updatedAt = parseIsoDate(row.get(CSV_ROW_LAST_MODIFIED) ?: ""),
                    ),
                )
            }
        }

        return ImportContent(
            items = items,
            tags = emptyList(),
            unknownItems = 0,
        )
    }

    override fun parseXmlDate(date: String): Long? {
        return parseNetDate(date)
    }
}